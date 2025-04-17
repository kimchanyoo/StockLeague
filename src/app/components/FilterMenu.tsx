'use client';

import { useEffect, useRef, useState } from 'react';
import styles from "@/app/styles/components/FilterMenu.module.css";
import ListIcon from "@mui/icons-material/List";

const filters = ['전체종목', '인기종목', '관심종목'];

export default function FilterMenu({
  selected,
  onChange,
}: {
  selected: string;
  onChange: (filter: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  // 바깥 클릭 시 닫기
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };

    if (open) {
      document.addEventListener('mousedown', handleClickOutside);
    } else {
      document.removeEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [open]);

  return (
    <div className={styles.container} ref={menuRef}>
      <button onClick={() => setOpen(prev => !prev)}>
        <ListIcon />
      </button>
      {open && (
        <ul className={styles.filterTool}>
          {filters.map((item) => (
            <li
              key={item}
              className={`${styles.index} ${item === selected ? styles.selected : ''}`}
              onClick={() => {
                onChange(item);
                setOpen(false);
              }}
            >
              {item}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
