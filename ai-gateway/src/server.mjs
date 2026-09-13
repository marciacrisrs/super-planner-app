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
        title: { type: ["string", "null"] },
        durationMinutes: { type: ["integer", "null"] },
        date: { type: ["string", "null"] },
        startTime: { type: ["string", "null"] },
        priority: { type: ["string", "null"], enum: ["REQUIRED", "IMPORTANT", "DESIRABLE", "LEISURE", null] },
        energy: { type: ["string", "null"], enum: ["LOW", "MEDIUM", "HIGH", null] },
      },
      required: [
        "message",
        "activityId",
        "evidence",
        "fields",
        "delayMinutes",
        "title",
        "durationMinutes",
        "date",
        "startTime",
        "priority",
        "energy",
      ],
    },
  },
  required: ["commandType", "explanation", "requiresConfirmation", "payload"],
};

const instructions = `You are the interpretation layer of Super Planner. Never invent planner state. Never decide scheduling rules. Never claim an action happened. Return only the structured proposal. Material changes require confirmation. Use only the supplied minimal context.

For CREATE_ACTIVITY_DRAFT, extract only facts expressed or unambiguously implied by the user's message. Return ISO date when a date is specified, 24-hour HH:mm when a start time is specified, duration in minutes, and one of the allowed priority/energy values only when justified. If a field is not known, return null. The title must be concise and describe the activity itself, not scheduling instructions. Never move an activity earlier than a stated start time.`;

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
