import { usePathname } from "next/navigation";
import Link from "next/link";
import styles from "@/app/styles/components/Sidebar.module.css";

const Sidebar = () => {
  const pathname = usePathname();

  return (
    <div className={styles.sidebar}>
      <ul>
        <li><Link href="/admin/dashboard" className={pathname.startsWith("/admin/dashboard") ? styles.active : ""}>공지 관리</Link></li>
        <li><Link href="/admin/dashboard" className={pathname.startsWith("/admin/dashboard") ? styles.active : ""}>문의 관리</Link></li>
        <li><Link href="/admin/dashboard" className={pathname.startsWith("/admin/dashboard") ? styles.active : ""}>신고 관리</Link></li>
        <li><Link href="/admin/dashboard" className={pathname.startsWith("/admin/dashboard") ? styles.active : ""}>댓글 관리</Link></li>
        <li><Link href="/admin/users" className={pathname.startsWith("/admin/users") ? styles.active : ""}>사용자 관리</Link></li>
        <li><Link href="/admin/settings" className={pathname.startsWith("/admin/settings") ? styles.active : ""}>설정</Link></li>
      </ul>
    </div>
  );
};

export default Sidebar;
