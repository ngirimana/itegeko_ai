"use client";

import { Alert, Box, Container, SimpleGrid } from "@mantine/core";
import { AppHeader } from "@/src/components/layout/AppHeader";
import { useAuth } from "@/src/features/auth/useAuth";
import { AdminLawPanel } from "./AdminLawPanel";
import { HeroSection } from "./HeroSection";
import { QuestionPanel } from "./QuestionPanel";
import { ReadinessBand } from "./ReadinessBand";
import { SearchPanel } from "./SearchPanel";
import { useLegalAssistant } from "../hooks/useLegalAssistant";

export function LegalAssistantPage() {
  const assistant = useLegalAssistant();
  const auth = useAuth();

  return (
    <Container component="main" size="xl" py={{ base: "lg", md: "xl" }}>
      <AppHeader
        session={auth.session}
        authLoading={auth.loading}
        onLogin={auth.login}
        onLogout={auth.logout}
      />
      <HeroSection />
      {assistant.error ? (
        <Alert color="red" variant="light" mb="md" title="Request failed">
          {assistant.error}
        </Alert>
      ) : null}
      <SimpleGrid cols={{ base: 1, md: 2 }} spacing="lg">
        <SearchPanel
          query={assistant.query}
          loading={assistant.loading}
          results={assistant.results}
          onQueryChange={assistant.setQuery}
          onSearch={assistant.runSearch}
        />
        <QuestionPanel
          question={assistant.question}
          answer={assistant.answer}
          loading={assistant.loading}
          onQuestionChange={assistant.setQuestion}
          onAsk={assistant.askQuestion}
        />
      </SimpleGrid>
      <Box mt="lg">
        <AdminLawPanel
          session={auth.session}
          authError={auth.error}
          getAccessToken={auth.getAccessToken}
          onCreated={assistant.runSearch}
        />
      </Box>
      <ReadinessBand />
    </Container>
  );
}
