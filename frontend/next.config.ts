import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${process.env.NEXT_PUBLIC_LEGAL_API_BASE_URL || "http://legal-service:8080"}/api/:path*`
      },
      {
        source: "/identity-api/:path*",
        destination: `${process.env.NEXT_PUBLIC_IDENTITY_API_BASE_URL || "http://identity-service:8082"}/api/:path*`
      }
    ];
  }
};

export default nextConfig;
