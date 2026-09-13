# Super Planner AI Gateway

Remote interpretation layer for Super Planner.

## Architecture

`Android → AI Gateway → LLM provider`

The gateway owns provider credentials. The Android app never receives the LLM API key.

## Endpoints

- `GET /health`
- `POST /v1/ai/propose`

### Request

```json
{
  "message": "amanhã preciso estudar francês por uma hora",
  "context": {
    "nowIso": "2026-09-13T20:00:00-03:00",
    "activeActivityId": null,
    "minimalRouteFacts": []
  }
}
```

### Environment

- `OPENAI_API_KEY` — required by the server
- `OPENAI_MODEL` — optional; defaults to `gpt-5.6-luna`
- `AI_GATEWAY_TOKEN` — optional bearer token for the mobile client
- `PORT` — optional; defaults to `8080`

The gateway uses the Responses API with Structured Outputs so the LLM returns a validated proposal rather than free-form state. The provider/model can be replaced without changing the Planner domain contract.

## Security rules

- Never accept database credentials from the client.
- Never let the LLM write directly to Room or any persistent store.
- Send only the minimum context required for the requested operation.
- Keep `store: false` for interpretation requests unless a future privacy decision explicitly changes this.
- Validate and authorize commands again in the app/domain before execution.
