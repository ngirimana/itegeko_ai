import { Button, Code, Group, ScrollArea, Stack, Text, Textarea, ThemeIcon, Title } from "@mantine/core";
import { Sparkles } from "lucide-react";
import { Panel } from "@/src/components/ui/Panel";

type QuestionPanelProps = {
  question: string;
  answer: string;
  loading: boolean;
  onQuestionChange: (question: string) => void;
  onAsk: () => void;
};

export function QuestionPanel({ question, answer, loading, onQuestionChange, onAsk }: QuestionPanelProps) {
  return (
    <Panel>
      <Stack gap="md">
        <Group gap="xs">
          <ThemeIcon color="teal" variant="light" size="md">
            <Sparkles size={18} />
          </ThemeIcon>
          <Title order={3} size="h4">
            Source-Backed Q&A
          </Title>
        </Group>
        <Textarea
          aria-label="Legal question"
          value={question}
          onChange={(event) => onQuestionChange(event.currentTarget.value)}
          minRows={5}
          autosize
        />
        <Button onClick={onAsk} disabled={loading} loading={loading} leftSection={<Sparkles size={16} />}>
        Ask AI/RAG Service
        </Button>
      {answer ? (
          <ScrollArea.Autosize mah={300}>
            <Code block bg="teal.0" c="dark.7" p="md" style={{ whiteSpace: "pre-wrap" }}>
              {answer}
            </Code>
          </ScrollArea.Autosize>
      ) : (
          <Text c="dimmed">Answers should include source grounding and disclaimers.</Text>
      )}
      </Stack>
    </Panel>
  );
}
