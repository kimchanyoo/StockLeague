"use client";
import styles from "@/app/styles/components/utills/TabMenu.module.css";

interface TabMenuProps {
  tabs: string[];
  activeTab: string;
  onTabChange: (tab: string) => void;
  tabTextSize?: string;
}

const TabMenu = ({ tabs, activeTab, onTabChange, tabTextSize = "16px"}: TabMenuProps) => {
  return (
    <div className={styles.tabs}>
      {tabs.map((tab) => (
        <button
          key={tab}
          className={`${styles.tab} ${activeTab === tab ? styles.activeTab : ""}`}
          onClick={() => onTabChange(tab)}
           style={{ fontSize: tabTextSize }}
        >
          {tab}
        </button>
      ))}
    </div>
  );
};

export default TabMenu;