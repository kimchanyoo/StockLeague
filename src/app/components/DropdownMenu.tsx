"use client";

import React, { useState } from "react";
import styles from "@/app/styles/components/DropdownMenu.module.css";
import { motion } from "framer-motion";

const menuItems = [
    {
        title: "종목/거래",
        submenu: [
            { label: "거래소", href: "/stocks/trade" },
            { label: "주식목록", href: "/stocks/stockList" },
        ],
    },
    {
        title: "고객지원/이용안내",
        submenu: [
            { label: "이용안내", href: "/help/guide" },
            { label: "공지사항", href: "/help/notice" },
            { label: "1:1문의하기", href: "/help/inquiry" },
            { label: "FAQ", href: "#" },
        ],
    },
    {
        title: "랭킹",
        submenu: [
            { label: "일일 랭킹", href: "#" },
        ],
    },
];

export default function DropdownMenu() {
    const [activeMenu, setActiveMenu] = useState<string | null>(null);

    const handleMouseEnter = (menu: string) => {
        setActiveMenu(menu);
    };

    const handleMouseLeave = () => {
        setActiveMenu(null);
    };

    return (
        <div className={styles.menu}>
            {menuItems.map((item) => (
                <div 
                    key={item.title} 
                    className={styles.menuItem} 
                    onMouseEnter={() => handleMouseEnter(item.title)} 
                    onMouseLeave={handleMouseLeave}
                >
                    <a>{item.title}</a>
                    {activeMenu === item.title && (
                        <motion.div 
                            className={styles.dropdown} 
                            initial={{ opacity: 0, y: -10 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -10 }}
                            transition={{ duration: 0.2 }}
                        >
                            {item.submenu.map((sub, index) => (
                                <a key={index} href={sub.href}>{sub.label}</a>
                            ))}
                        </motion.div>
                    )}
                </div>
            ))}
        </div>
    );
}
