import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { ListToolsRequestSchema, CallToolRequestSchema } from "@modelcontextprotocol/sdk/types.js";
import fs from "fs/promises";
import path from "path";
import { fileURLToPath } from "url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
// Root directory where REQ-MDNT-001.md is located
const PROJECT_ROOT = path.resolve(__dirname, "../../..");

const server = new Server(
  { name: "medianet-base-mcp", version: "1.0.0" },
  { capabilities: { tools: {} } }
);

server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "get_project_requirements",
        description: "Devuelve los requerimientos del proyecto contenidos en REQ-MDNT-001.md",
        inputSchema: {
          type: "object",
          properties: {},
          required: []
        }
      },
      {
        name: "get_database_schemas",
        description: "Devuelve información sobre los esquemas de base de datos requeridos por la arquitectura.",
        inputSchema: {
          type: "object",
          properties: {},
          required: []
        }
      }
    ]
  };
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  switch (request.params.name) {
    case "get_project_requirements": {
      try {
        const reqPath = path.join(PROJECT_ROOT, "REQ-MDNT-001.md");
        const content = await fs.readFile(reqPath, "utf-8");
        return {
          content: [{ type: "text", text: content }]
        };
      } catch (e) {
        return {
          isError: true,
          content: [{ type: "text", text: `Error leyendo requerimientos: ${e.message}` }]
        };
      }
    }
    case "get_database_schemas": {
      const dbInfo = `
Esquemas de Base de Datos Postgres Requeridos:
1. db-clients: Contiene la tabla Cliente (hereda de Persona).
2. db-accounts: Contiene la tabla Cuenta.
3. db-movements: Contiene la tabla Movimiento.

Cada esquema debe incluir obligatoriamente:
- Tabla de historial
- Tabla de auditoría
- Tabla de eventos
- Tabla de errores
      `;
      return {
        content: [{ type: "text", text: dbInfo }]
      };
    }
    default:
      throw new Error("Tool not found");
  }
});

const transport = new StdioServerTransport();
server.connect(transport).then(() => {
  console.error("Medianet Base MCP Server running on stdio");
});
