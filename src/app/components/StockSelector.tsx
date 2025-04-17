"use client";

import React, {useState} from "react";
import styles from "@/app/styles/components/StockSelector.module.css";
import FilterMenu from "./FilterMenu";
import SearchIcon from "@mui/icons-material/Search";
import { color } from "framer-motion";
import MiniStockList from "./MiniStockList";

const dummyStocks = [
  { id: 1, name: '삼성전자', stockCode: '005930', currentPrice: 72000, priceChange: 1.8 },
  { id: 2, name: '카카오', stockCode: '035720', currentPrice: 51000, priceChange: -2.3 },
  { id: 3, name: '네이버', stockCode: '035420', currentPrice: 182000, priceChange: 0.0 },
];

export default function StockSelector() {
    
  const [selectedFilter, setSelectedFilter] = useState('전체종목');

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.left}>
          <FilterMenu selected={selectedFilter} onChange={setSelectedFilter} />
        </div>
        <h1 className={styles.center}>{selectedFilter}</h1>
      </div>
      <div className={styles.search}>
        <div className={styles.searchBox}>
          <input/>
          <button>
            <SearchIcon style={{color: '#999999'}}/>
          </button>
        </div>
      </div>
      <div className={styles.miniStockList}>
        <MiniStockList stocks={dummyStocks}/>
      </div>
    </div>
  );
}