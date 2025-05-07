import { AuthProvider } from "@/context/AuthContext"; // AuthContext에서 제공하는 AuthProvider

export const Providers = ({ children }: { children: React.ReactNode }) => {
  return <AuthProvider>{children}</AuthProvider>;
};
