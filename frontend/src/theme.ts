import { createTheme, rem } from "@mantine/core";

export const theme = createTheme({
  primaryColor: "teal",
  defaultRadius: "sm",
  fontFamily:
    "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, sans-serif",
  headings: {
    fontFamily:
      "Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, sans-serif",
    fontWeight: "800"
  },
  components: {
    Button: {
      defaultProps: {
        radius: "sm"
      }
    },
    Card: {
      defaultProps: {
        radius: "sm",
        withBorder: true
      }
    },
    TextInput: {
      defaultProps: {
        radius: "sm"
      }
    },
    Textarea: {
      defaultProps: {
        radius: "sm"
      }
    }
  },
  spacing: {
    xs: rem(8),
    sm: rem(12),
    md: rem(18),
    lg: rem(24),
    xl: rem(36)
  }
});
