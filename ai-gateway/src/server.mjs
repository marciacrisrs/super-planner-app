import http from "node:http";
import OpenAI from "openai";

const port = Number(process.env.PORT ?? 8080);
const model = process.env.OPENAI_MODEL ?? "gpt-5.6-luna";
const gatewayToken = process.env.AI_GATEWAY_TOKEN;
const client = new OpenAI({ apiKey: process.env.OPENAI_API_KEY });

const proposalSchema = {
  type: "object",
  additionalProperties: false,
  properties: {
    commandType: {
      type: "string",
      enum: ["CREATE_ACTIVITY_DRAFT", "REORGANIZE_DAY", "EXPLAIN_NEXT_ACTIVITY", "MISSING_INFORMATION", "RECALCULATE_ROUTE"],
    },
    explanation: { type: "string" },
    requiresConfirmation: { type: "boolean" },
    payload: {
      type: "object",
      additionalProperties: false,
      properties: {
        message: { type: "string" },
        activityId: { type: ["string", "null"] },
        evidence: { type: "array", items: { type: "string" } },
        fields: { type: "array", items: { type: "string" } },
        delayMinutes: { type: ["integer", "null"] },
      },
      required: ["message", "activityId", "evidence", "fields", "delayMinutes"],
    },
  },
  required: ["commandType", "explanation", "requiresConfirmation", "payload"],
};

const instructions = `You are the interpretation layer of Super Planner. Never invent planner state. Never decide scheduling rules. Never claim an action happened. Return only the structured proposal. Material changes require confirmation. Use only the supplied minimal context.`;

function send(res, status, body) {
  res.writeHead(status, { "content-type": "application/json; charset=utf-8" });
  res.end(JSON.stringify(body));
}

async function propose(input) {
  const response = await client.responses.create({
    model,
    store: false,
    instructions,
    input: JSON.stringify({ message: input.message, context: input.context ?? {} }),
    text: {
      format: {
        type: "json_schema",
        name: "super_planner_ai_proposal",
        strict: true,
        schema: proposalSchema,
      },
    },
  });

  return JSON.parse(response.output_text);
}

const server = http.createServer(async (req, res) => {
  if (req.method === "GET" && req.url === "/health") {
    return send(res, 200, { status: "ok", service: "super-planner-ai-gateway", model });
  }

  if (req.method !== "POST" || req.url !== "/v1/ai/propose") {
    return send(res, 404, { error: "not_found" });
  }

  if (gatewayToken && req.headers.authorization !== `Bearer ${gatewayToken}`) {
    return send(res, 401, { error: "unauthorized" });
  }

  try {
    let raw = "";
    for await (const chunk of req) raw += chunk;
    const input = JSON.parse(raw);

    if (typeof input.message !== "string" || input.message.trim().length === 0) {
      return send(res, 400, { error: "message_required" });
    }

    const proposal = await propose(input);
    return send(res, 200, { proposal, model });
  } catch (error) {
    console.error("ai_gateway_error", error);
    return send(res, 502, { error: "ai_provider_unavailable" });
  }
});

server.listen(port, () => {
  console.log(`Super Planner AI Gateway listening on :${port}`);
});
