# Development Agent / Skills

## Available skills

| Skill | Purpose |
|-------|---------|
| `scaffold-springboot-hexagonal` | Bootstrap a hexagonal Spring Boot module |
| `scaffold-angular-hexagonal` | Bootstrap an Angular feature module |

## Run Spring scaffold

```bash
cd ai/agent/skills/scaffold-springboot-hexagonal/scripts
chmod +x init_spring.sh
./init_spring.sh my-new-service
```

## Run Angular scaffold

```bash
cd ai/agent/skills/scaffold-angular-hexagonal/scripts
chmod +x init_angular.sh
./init_angular.sh my-feature
```

## MCP helper (optional)

The `mcp-base-info` folder contains an MCP server prototype for project metadata. Install with `pnpm install` inside that directory before running.
