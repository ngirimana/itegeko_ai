import { Badge, Group, Paper, Stack, Text, Title } from "@mantine/core";
import { STACK_LABELS } from "@/src/constants/app.constants";

export function HeroSection() {
  return (
    <Paper component="section" bg="transparent" mb="xl" pb="xl" radius={0} style={{ borderBottom: "1px solid var(--mantine-color-gray-3)" }}>
      <Group align="flex-start" justify="space-between" gap="xl">
        <Stack gap="sm" maw={980}>
          <Title order={2} size="clamp(1.7rem, 3vw, 3rem)" lh={1.05}>
            Search, verify, and ask questions about Rwanda law with source-backed answers.
          </Title>
          <Text c="dimmed" size="lg" maw={780}>
          Built for article-level retrieval, citations, legal status warnings, reviewer workflows,
          and institutional-grade security.
          </Text>
        </Stack>
        <Group gap="xs" maw={380}>
        {STACK_LABELS.map((label) => (
          <Badge key={label} color="teal" variant="outline" radius="xl" size="lg">
            {label}
          </Badge>
        ))}
        </Group>
      </Group>
    </Paper>
  );
}
