# Bootcamp Task M13: Spring AI with Ollama

A Spring Boot chat application that stores conversations in SQLite and uses a
local Ollama model through Spring AI.

## Prerequisites

- Java 21
- [Ollama](https://ollama.com/download)

Download the default model:

```powershell
ollama pull llama3.2
```

You can test the model interactively with `ollama run llama3.2` and exit with
`/bye`. Ollama serves its local API at `http://localhost:11434`.

## Configuration and startup

The application defaults to `http://localhost:11434` and the `llama3.2` model.
Copy `.env.example` to `.env` if you want to override these settings. This
PowerShell command leaves an existing `.env` untouched:

```powershell
if (-not (Test-Path .env)) {
  Copy-Item .env.example .env
}
```

Available settings are `OLLAMA_BASE_URL`, `OLLAMA_MODEL`, `AI_SYSTEM_PROMPT`,
and the optional `SQLITE_DB_PATH`. No API key is required.

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

## Chat API

Create a conversation and note the returned `id`:

```powershell
curl.exe -X POST http://localhost:8080/api/chats `
  -H "Content-Type: application/json" `
  -d '{"title":"Spring AI demo"}'
```

Send a message, replacing `CHAT_ID` with that id:

```powershell
curl.exe -X POST "http://localhost:8080/api/chats/CHAT_ID/chatMessages" `
  -H "Content-Type: application/json" `
  -d '{"content":"Explain what a Spring Bean is in two sentences."}'
```

The response contains the conversation, including the user message and the
assistant's reply.

## Tests

Run the test suite with:

```powershell
.\mvnw.cmd test
```

The client tests mock Spring AI, and the context-load test uses an unreachable
Ollama URL, so tests do not require Ollama to be running.
