import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  devIndicators: false,
  env: {
    API_SECRET_KEY: process.env.API_SECRET_KEY,
  },
  async rewrites() {
    return [
      {
        source: "/api/:path*", // 클라이언트가 요청할 경로
        destination: `${process.env.API_BASE_URL}/api/:path*`, // 실제 백엔드 서버 주소
      },
    ];
  },
};

export default nextConfig;