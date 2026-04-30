import { Group, Paper, Text, ThemeIcon } from "@mantine/core";
import { BookOpen } from "lucide-react";

export function ReadinessBand() {
  return (
    <Paper component="section" withBorder shadow="xs" p="lg" mt="xl" radius="sm">
      <Group align="center" gap="sm">
        <ThemeIcon color="teal" variant="light" size="lg">
          <BookOpen size={20} />
        </ThemeIcon>
        <Text c="dimmed">
        This stack is ready for verified legal content, embeddings in pgvector, private document
        storage in MinIO, and JWT/SSO through Keycloak.
        </Text>
      </Group>
    </Paper>
  );
}
