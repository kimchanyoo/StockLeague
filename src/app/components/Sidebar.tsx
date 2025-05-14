import { usePathname } from "next/navigation";
import Link from "next/link";
import styles from "@/app/styles/components/Sidebar.module.css";

const Sidebar = () => {
  const pathname = usePathname();

  return (
    <div className={styles.sidebar}>
      <ul>
        <li>
          <Link
            href="/admin"
            className={pathname === "/admin" ? styles.active : ""}
          >
            대시보드
          </Link>
        </li>
        <li>
          <Link
            href="/admin/notices"
            className={pathname.startsWith("/admin/notices") ? styles.active : ""}
          >
            공지 관리
          </Link>
        </li>
        <li>
          <Link
            href="/admin/inquiries"
            className={pathname.startsWith("/admin/inquiries") ? styles.active : ""}
          >
            문의 관리
          </Link>
        </li>
        <li>
          <Link
            href="/admin/reports"
            className={pathname.startsWith("/admin/reports") ? styles.active : ""}
          >
            신고 관리
          </Link>
        </li>
        <li>
          <Link
            href="/admin/comments"
            className={pathname.startsWith("/admin/comments") ? styles.active : ""}
          >
            댓글 관리
          </Link>
        </li>
        <li>
          <Link
            href="/admin/users"
            className={pathname.startsWith("/admin/users") ? styles.active : ""}
          >
            사용자 관리
          </Link>
        </li>
      </ul>
    </div>
  );
};

export default Sidebar;
