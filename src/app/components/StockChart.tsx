"use client";

import React, {useState} from "react";
import styles from "@/app/styles/components/StockChart.module.css";

export default function StockChart({
  activeTab,
  setActiveTab,
}: {
  activeTab: 'chart' | 'community';
  setActiveTab: (tab: 'chart' | 'community') => void;
}) {

  return (
    <div className={styles.container}>
      <div className={styles.centerSection}>

      </div>

      <div className={styles.bottomSection}>

      </div>
    </div>
  );
}