import { Badge, Box, Button, Group, Stack, Text, ThemeIcon, Title } from "@mantine/core";
import { LogIn, LogOut, ShieldCheck } from "lucide-react";
import { APP_NAME, APP_TAGLINE, LEGAL_INFORMATION_NOTICE } from "@/src/constants/app.constants";
import { isAdmin, type AuthSession } from "@/src/features/auth/auth.service";

type AppHeaderProps = {
  session: AuthSession | null;
  authLoading: boolean;
  onLogin: () => void;
  onLogout: () => void;
};

export function AppHeader({ session, authLoading, onLogin, onLogout }: AppHeaderProps) {
  return (
    <Box component="header" mb="xl">
      <Group justify="space-between" align="flex-start" gap="md">
        <Stack gap={4}>
          <Text c="teal.8" fw={800} size="xs" tt="uppercase">
            {APP_TAGLINE}
          </Text>
          <Title order={1} size="clamp(2rem, 4vw, 3.2rem)" lh={1}>
            {APP_NAME}
          </Title>
        </Stack>
        <Group gap="sm" justify="flex-end">
          <Badge
            color="teal"
            variant="light"
            size="lg"
            radius="xl"
            leftSection={
              <ThemeIcon color="teal" variant="transparent" size={18}>
                <ShieldCheck size={16} />
              </ThemeIcon>
            }
          >
            {LEGAL_INFORMATION_NOTICE}
          </Badge>
          {session ? (
            <>
              <Badge color={isAdmin(session) ? "teal" : "gray"} variant="outline">
                {isAdmin(session) ? "Admin" : session.email}
              </Badge>
              <Button variant="light" color="gray" onClick={onLogout} leftSection={<LogOut size={16} />}>
                Sign out
              </Button>
            </>
          ) : (
            <Button onClick={onLogin} loading={authLoading} leftSection={<LogIn size={16} />}>
              Admin sign in
            </Button>
          )}
        </Group>
      </Group>
    </Box>
  );
}
