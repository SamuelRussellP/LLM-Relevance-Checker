from fastapi import FastAPI, Body
from pydantic import BaseModel
import requests
import json
import uvicorn

app = FastAPI()

class GenRequest(BaseModel):
    prompt: str
    chatbot_reply: str

@app.post("/generate-response")
def generate_followup(payload: GenRequest):
    ollama_prompt = f"""
{payload.prompt}

Chatbot replied: {payload.chatbot_reply}

Now write a natural-sounding follow-up response from the user's perspective.
""".strip()

    try:
        response = requests.post(
            "http://localhost:11434/api/generate",
            json={"model": "mistral", "prompt": ollama_prompt},
            stream=True  # ✅ REQUIRED for Ollama streaming output
        )

        generated = ""

        for line in response.iter_lines():
            if line:
                try:
                    chunk = json.loads(line.decode("utf-8"))
                    if "response" in chunk:
                        generated += chunk["response"]
                except json.JSONDecodeError as e:
                    print(f"⚠️ Skipping malformed line: {line} ({e})")
                    continue

        return {"generated_response": generated.strip()}

    except Exception as e:
        print("❌ ERROR:", e)
        return {"error": str(e)}

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8001)
