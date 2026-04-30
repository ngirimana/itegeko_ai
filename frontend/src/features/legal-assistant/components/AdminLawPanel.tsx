import { useEffect, useMemo, useState } from "react";
import {
  ActionIcon,
  Alert,
  Button,
  Group,
  Select,
  SimpleGrid,
  Stack,
  Text,
  Textarea,
  TextInput,
  ThemeIcon,
  Title
} from "@mantine/core";
import { FilePlus2, LockKeyhole, Plus, Save, Trash2 } from "lucide-react";
import { Panel } from "@/src/components/ui/Panel";
import { isAdmin, type AuthSession } from "@/src/features/auth/auth.service";
import { LegalApiFetchService } from "@/src/services/legal-api.service";
import type {
  CreateLegalArticleRequest,
  CreateLegalDocumentRequest,
  LegalCatalogResponse
} from "../types/legal.types";

type AdminLawPanelProps = {
  session: AuthSession | null;
  authError: string;
  getAccessToken: () => Promise<string | null>;
  onCreated: () => void;
};

type LawFormState = {
  title: string;
  lawNumber: string;
  publicationDate: string;
  language: string;
  status: string;
  sourceUrl: string;
  categoryId: string;
  sourceId: string;
  articles: CreateLegalArticleRequest[];
};

const emptyForm: LawFormState = {
  title: "",
  lawNumber: "",
  publicationDate: "",
  language: "English",
  status: "active",
  sourceUrl: "",
  categoryId: "",
  sourceId: "",
  articles: [{ articleNumber: "Article 1", articleTitle: "", articleText: "" }]
};

const legalApi = new LegalApiFetchService();

export function AdminLawPanel({ session, authError, getAccessToken, onCreated }: AdminLawPanelProps) {
  const [catalog, setCatalog] = useState<LegalCatalogResponse>({ categories: [], sources: [] });
  const [form, setForm] = useState<LawFormState>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const canManageLaws = isAdmin(session);

  useEffect(() => {
    if (!canManageLaws) {
      setCatalog({ categories: [], sources: [] });
      return;
    }
    let mounted = true;
    legalApi.catalog()
      .then((payload) => {
        if (mounted) {
          setCatalog(payload);
        }
      })
      .catch((caught) => {
        if (mounted) {
          setError(caught instanceof Error ? caught.message : "Could not load legal catalog.");
        }
      });
    return () => {
      mounted = false;
    };
  }, [canManageLaws]);

  const categoryOptions = useMemo(
    () => catalog.categories.map((category) => ({ value: category.id, label: category.name })),
    [catalog.categories]
  );
  const sourceOptions = useMemo(
    () => catalog.sources.map((source) => ({ value: source.id, label: source.name })),
    [catalog.sources]
  );

  async function saveLaw() {
    setSaving(true);
    setMessage("");
    setError("");
    try {
      const accessToken = await getAccessToken();
      if (!accessToken) {
        throw new Error("Please sign in as an admin first.");
      }
      if (!canManageLaws) {
        throw new Error("Your account does not have the ADMIN role.");
      }
      const response = await legalApi.createLegalDocument(toRequest(form), accessToken);
      setMessage(
        response.indexingStatus === "indexed"
          ? `Saved law and indexed ${response.indexedArticles} articles.`
          : "Saved law, but indexing needs to be retried."
      );
      setForm(emptyForm);
      onCreated();
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Could not save the law.");
    } finally {
      setSaving(false);
    }
  }

  function updateArticle(index: number, updates: Partial<CreateLegalArticleRequest>) {
    setForm((current) => ({
      ...current,
      articles: current.articles.map((article, articleIndex) =>
        articleIndex === index ? { ...article, ...updates } : article
      )
    }));
  }

  function addArticle() {
    setForm((current) => ({
      ...current,
      articles: [
        ...current.articles,
        { articleNumber: `Article ${current.articles.length + 1}`, articleTitle: "", articleText: "" }
      ]
    }));
  }

  function removeArticle(index: number) {
    setForm((current) => ({
      ...current,
      articles: current.articles.filter((_article, articleIndex) => articleIndex !== index)
    }));
  }

  if (!session) {
    return authError ? (
      <Panel>
        <Alert color="red" variant="light">{authError}</Alert>
      </Panel>
    ) : null;
  }

  if (!canManageLaws) {
    return (
      <Panel>
        <Stack gap="md">
          <Group gap="xs">
            <ThemeIcon color="gray" variant="light" size="md">
              <LockKeyhole size={18} />
            </ThemeIcon>
            <Title order={3} size="h4">
              Add Laws
            </Title>
          </Group>
          <Alert color="yellow" variant="light">This account cannot add legal content.</Alert>
        </Stack>
      </Panel>
    );
  }

  return (
    <Panel>
      <Stack gap="md">
        <Group gap="xs">
          <ThemeIcon color="teal" variant="light" size="md">
            {canManageLaws ? <FilePlus2 size={18} /> : <LockKeyhole size={18} />}
          </ThemeIcon>
          <Title order={3} size="h4">
            Add Laws
          </Title>
        </Group>

        {authError ? <Alert color="red" variant="light">{authError}</Alert> : null}
        {message ? <Alert color="teal" variant="light">{message}</Alert> : null}
        {error ? <Alert color="red" variant="light">{error}</Alert> : null}

        <SimpleGrid cols={{ base: 1, sm: 2 }} spacing="sm">
          <TextInput
            label="Law title"
            value={form.title}
            onChange={(event) => setForm({ ...form, title: event.currentTarget.value })}
            disabled={!canManageLaws}
          />
          <TextInput
            label="Law number"
            value={form.lawNumber}
            onChange={(event) => setForm({ ...form, lawNumber: event.currentTarget.value })}
            disabled={!canManageLaws}
          />
          <TextInput
            label="Publication date"
            type="date"
            value={form.publicationDate}
            onChange={(event) => setForm({ ...form, publicationDate: event.currentTarget.value })}
            disabled={!canManageLaws}
          />
          <Select
            label="Status"
            data={["active", "verified", "amended", "unverified"]}
            value={form.status}
            onChange={(value) => setForm({ ...form, status: value || "active" })}
            disabled={!canManageLaws}
          />
          <Select
            label="Category"
            data={categoryOptions}
            value={form.categoryId || null}
            onChange={(value) => setForm({ ...form, categoryId: value || "" })}
            disabled={!canManageLaws}
            clearable
          />
          <Select
            label="Source"
            data={sourceOptions}
            value={form.sourceId || null}
            onChange={(value) => setForm({ ...form, sourceId: value || "" })}
            disabled={!canManageLaws}
            clearable
          />
        </SimpleGrid>

        <TextInput
          label="Source URL"
          value={form.sourceUrl}
          onChange={(event) => setForm({ ...form, sourceUrl: event.currentTarget.value })}
          disabled={!canManageLaws}
        />

        <Stack gap="sm">
          <Group justify="space-between">
            <Text fw={800}>Articles</Text>
            <Button
              variant="light"
              onClick={addArticle}
              disabled={!canManageLaws}
              leftSection={<Plus size={16} />}
            >
              Add article
            </Button>
          </Group>
          {form.articles.map((article, index) => (
            <Stack key={index} gap="xs" p="sm" style={{ border: "1px solid var(--mantine-color-gray-3)", borderRadius: 6 }}>
              <Group align="flex-end">
                <TextInput
                  label="Article number"
                  value={article.articleNumber}
                  onChange={(event) => updateArticle(index, { articleNumber: event.currentTarget.value })}
                  disabled={!canManageLaws}
                  flex={1}
                />
                <ActionIcon
                  variant="light"
                  color="red"
                  aria-label="Remove article"
                  onClick={() => removeArticle(index)}
                  disabled={!canManageLaws || form.articles.length === 1}
                >
                  <Trash2 size={16} />
                </ActionIcon>
              </Group>
              <TextInput
                label="Article title"
                value={article.articleTitle}
                onChange={(event) => updateArticle(index, { articleTitle: event.currentTarget.value })}
                disabled={!canManageLaws}
              />
              <Textarea
                label="Article text"
                value={article.articleText}
                onChange={(event) => updateArticle(index, { articleText: event.currentTarget.value })}
                disabled={!canManageLaws}
                autosize
                minRows={4}
              />
            </Stack>
          ))}
        </Stack>

        <Button onClick={saveLaw} loading={saving} disabled={!canManageLaws} leftSection={<Save size={16} />}>
          Save law
        </Button>
      </Stack>
    </Panel>
  );
}

function toRequest(form: LawFormState): CreateLegalDocumentRequest {
  return {
    title: form.title.trim(),
    lawNumber: optional(form.lawNumber),
    publicationDate: optional(form.publicationDate),
    language: form.language.trim() || "English",
    status: form.status.trim() || "active",
    sourceUrl: optional(form.sourceUrl),
    categoryId: optional(form.categoryId),
    sourceId: optional(form.sourceId),
    articles: form.articles.map((article) => ({
      articleNumber: article.articleNumber.trim(),
      articleTitle: optional(article.articleTitle),
      articleText: article.articleText.trim()
    }))
  };
}

function optional(value?: string): string | undefined {
  const trimmed = value?.trim();
  return trimmed ? trimmed : undefined;
}
