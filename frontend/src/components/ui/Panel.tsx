import type { ReactNode } from "react";
import { Card } from "@mantine/core";

type PanelProps = {
  children: ReactNode;
};

export function Panel({ children }: PanelProps) {
  return (
    <Card shadow="xs" p="lg">
      {children}
    </Card>
  );
}
