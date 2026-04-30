import { Badge, Button, Group, Stack, Text, TextInput, ThemeIcon, Title } from "@mantine/core";
import { Search } from "lucide-react";
import { Panel } from "@/src/components/ui/Panel";
import type { LegalArticleResult } from "../types/legal.types";

type SearchPanelProps = {
  query: string;
  loading: boolean;
  results: LegalArticleResult[];
  onQueryChange: (query: string) => void;
  onSearch: () => void;
};

export function SearchPanel({ query, loading, results, onQueryChange, onSearch }: SearchPanelProps) {
  return (
    <Panel>
      <Stack gap="md">
        <Group gap="xs">
          <ThemeIcon color="teal" variant="light" size="md">
            <Search size={18} />
          </ThemeIcon>
          <Title order={3} size="h4">
            Legal Search
          </Title>
        </Group>
        <Group align="flex-end" gap="sm">
          <TextInput
            aria-label="Search query"
            value={query}
            onChange={(event) => onQueryChange(event.currentTarget.value)}
            flex={1}
          />
          <Button onClick={onSearch} disabled={loading} loading={loading} leftSection={<Search size={16} />}>
          Search
          </Button>
        </Group>
        <Stack gap="sm">
        {results.length === 0 ? (
          <Text c="dimmed">Run a search to see legal articles.</Text>
        ) : (
          results.map((result) => (
            <Stack
              key={result.id}
              gap={6}
              p="sm"
              bg="teal.0"
              style={{ borderLeft: "4px solid var(--mantine-color-teal-6)", borderRadius: 4 }}
            >
              <Group justify="space-between" gap="xs">
                <Text fw={800}>{result.articleTitle || result.articleNumber}</Text>
                {result.status ? (
                  <Badge color={result.status === "verified" ? "teal" : "gray"} variant="light">
                    {result.status}
                  </Badge>
                ) : null}
              </Group>
              <Text c="dimmed" size="sm" lineClamp={4}>
                {result.articleText}
              </Text>
              <Text c="teal.8" size="xs" fw={700}>
                {result.document?.title || "Legal source"}
              </Text>
            </Stack>
          ))
        )}
        </Stack>
      </Stack>
    </Panel>
  );
}
