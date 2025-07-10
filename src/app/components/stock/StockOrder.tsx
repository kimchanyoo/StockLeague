"use client";

import React, { useState, useRef, useEffect } from "react";
import styles from "@/app/styles/components/stock/StockOrder.module.css";
import MyOrder from "../user/MyOrder";
import TabMenu from "../utills/TabMenu";
import RemoveIcon  from "@mui/icons-material/Remove";
import AddIcon from "@mui/icons-material/Add";
import { OrderbookData } from "@/lib/api/stock"
import { Client } from "@stomp/stompjs";
import { useAuth } from "@/context/AuthContext";

interface StockOrderProps {
  stockName: string;
  ticker: string; 
  currentPrice: number;
}

const StockOrder = ({ stockName, currentPrice, ticker }: StockOrderProps) => {
  const myMoney = 1000000;
  
  const [activeTab, setActiveTab] = useState<string>("체결 내역");
  const tabList = ["체결 내역", "미체결 내역"];
  const [useCurrentPrice, setUseCurrentPrice] = useState(false);
  const [orderType, setOrderType] = useState<"buy" | "sell">("buy");
  const [quantity, setQuantity] = useState(0);
  const [price, setPrice] = useState(currentPrice);
  const [priceInput, setPriceInput] = useState(currentPrice.toString());

  const scrollRef = useRef<HTMLDivElement>(null);
  const totalPrice = quantity * price;

  const [orderbook, setOrderbook] = useState<OrderbookData | null>(null);
  const { accessToken } = useAuth();

  useEffect(() => {
    if (!ticker) return;

    const client = new Client({
      webSocketFactory: () => new WebSocket(process.env.NEXT_PUBLIC_SOCKET_URL!),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`, 
      },
      reconnectDelay: 15_000,
      heartbeatIncoming: 10_000,
      heartbeatOutgoing: 10_000,

      onConnect: () => {
        client.subscribe(`/topic/orderbook/${ticker}`, (message) => {
          try {
            const data = JSON.parse(message.body) as OrderbookData;
            setOrderbook(data);
          } catch (err) {
            console.error("호가 데이터 처리 오류:", err);
          }
        });
      },

      onStompError: (frame) => {
        console.error("WebSocket STOMP 오류:", frame.headers["message"]);
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [ticker]);

  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;
    if (!accessToken) {
      console.warn("⚠️ accessToken 없음 - WebSocket 연결 건너뜀");
      return;
    }

    const allHogas = el.querySelectorAll("[data-price]");
    const currentPriceElement = Array.from(allHogas).find((div) => {
      return Number(div.getAttribute("data-price")) === currentPrice;
    });

    if (currentPriceElement) {
      currentPriceElement.scrollIntoView({
        behavior: "smooth",
        block: "center",
      });
    }
  }, [stockName, currentPrice]);

  useEffect(() => {
    if (useCurrentPrice) {
      setPrice(currentPrice);
      setPriceInput(currentPrice.toString());
    }
  }, [useCurrentPrice, currentPrice]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const orderData = {
      type: orderType,
      stock: stockName,
      quantity,
      price,
      totalPrice
    };
    console.log("주문 데이터:", orderData);
    // 서버 전송 처리 가능
  };

  const handleQuantityRatioChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const ratio = Number(e.target.value);
    if (price > 0) {
      const qty = Math.floor((myMoney * ratio) / price);
      setQuantity(qty);
    }
  };

  const handlePriceClick = (clickedPrice: number) => {
    setPrice(clickedPrice);
    setPriceInput(clickedPrice.toString());
    setUseCurrentPrice(false);
  };

  const handlePriceInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let raw = e.target.value.replace(/,/g, "");
    if (!/^\d*$/.test(raw)) return; // 숫자 외 입력 차단

    setPriceInput(raw);

    const number = parseInt(raw, 10);
    if (!isNaN(number)) {
      const adjusted = Math.floor(number / 10) * 10;
      setPrice(adjusted);
      setUseCurrentPrice(false);
    }
  };

  const handlePriceBlur = () => {
    setPriceInput(price.toLocaleString());
  };

  const bidOrders = orderbook?.bidPrices.map((price, i) => ({
    price,
    quantity: orderbook.bidVolumes[i],
  })) ?? [];

  const askOrders = orderbook?.askPrices.map((price, i) => ({
    price,
    quantity: orderbook.askVolumes[i],
  })) ?? [];

  // 최대값 (퍼센트 기준용)
  const maxBidQty = Math.max(...bidOrders.map(o => o.quantity), 1);
  const maxAskQty = Math.max(...askOrders.map(o => o.quantity), 1);
  const currentQty =
    bidOrders.find((o) => o.price === currentPrice)?.quantity ??
    askOrders.find((o) => o.price === currentPrice)?.quantity ?? 0;

  return (
    <div className={styles.orderContainer}>
      <h1 className={styles.title}>{stockName} 주식주문</h1>

      <div className={styles.myMoney}>
        <h1>보유자산</h1>
        <h1><span>{myMoney.toLocaleString()}</span>원</h1>
      </div>

      <div className={styles.orderContents}>
        {/* 왼쪽: 호가창 */}
        <div className={styles.quoteBox}>
          <div className={styles.scrollArea} ref={scrollRef}>
            {/* 매도 호가 (ask) - 위쪽 */}
            {askOrders.map((order, i) => {
              const percent = (order.quantity / maxAskQty) * 100;
              return (
                <div
                  key={`ask-${i}`}
                  data-price={order.price}
                  className={styles.ask}
                  onClick={() => handlePriceClick(order.price)}
                >
                  <div
                    className={styles.hogaOverlayBar}
                    style={{ width: `${percent}%`, backgroundColor: "#93B9E1" }}
                  />
                  <div className={styles.hogaOverlayRow}>
                    <span className={styles.hogaPrice}>{order.price.toLocaleString()}</span>
                    <span className={styles.hogaQty}>{order.quantity.toLocaleString()}</span>
                  </div>
                </div>
              );
            })}

            {/* 현재가 */}
            <div
              data-price={currentPrice}
              className={styles.centerLine}
              onClick={() => handlePriceClick(currentPrice)}
            >
              <div className={styles.hogaOverlayRow}>
                <span className={styles.hogaOverlayText}>{currentPrice.toLocaleString()}</span>
                <span className={styles.hogaQty}>{currentQty.toLocaleString()}</span>
              </div>
            </div>

            {/* 매수 호가 (bid) - 아래쪽 */}
            {bidOrders.map((order, i) => {
              const percent = (order.quantity / maxBidQty) * 100;
              return (
                <div
                  key={`bid-${i}`}
                  data-price={order.price}
                  className={styles.bid}
                  onClick={() => handlePriceClick(order.price)}
                >
                  <div
                    className={styles.hogaOverlayBar}
                    style={{ width: `${percent}%`, backgroundColor: "#F19999" }}
                  />
                  <div className={styles.hogaOverlayRow}>
                    <span className={styles.hogaPrice}>{order.price.toLocaleString()}</span>
                    <span className={styles.hogaQty}>{order.quantity.toLocaleString()}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* 오른쪽: 주문 폼 */}
        <form className={styles.orderForm} onSubmit={handleSubmit}>
          {/* 매수/매도 탭 */}
          <div className={styles.row}>
            <div className={styles.tabGroup}>
              <button
                type="button"
                className={`${styles.tab} ${orderType === "buy" ? styles.activeBid : ""}`}
                onClick={() => setOrderType("buy")}
              >
                매수
              </button>
              <button
                type="button"
                className={`${styles.tab} ${orderType === "sell" ? styles.activeAsk : ""}`}
                onClick={() => setOrderType("sell")}
              >
                매도
              </button>
            </div>
          </div>

          {/* 수량 */}
          <div className={styles.row}>
            <label>수량</label>
            <div className={styles.rowContainer}>
              <input
                type="text"
                value={quantity}
                onChange={(e) => {
                  const raw = e.target.value.replace(/[^0-9]/g, "");
                  setQuantity(Number(raw));
                }}
              />
              <select onChange={handleQuantityRatioChange} defaultValue="">
                <option value="" disabled>비율</option>
                <option value="0.1">10%</option>
                <option value="0.25">25%</option>
                <option value="0.5">50%</option>
                <option value="0.75">75%</option>
                <option value="1">100%</option>
              </select>
            </div>
          </div>

          {/* 가격 */}
          <div className={styles.row}>
            <label>가격</label>
            <div className={styles.rowContainer}>
              <input
                type="text"
                value={priceInput}
                onChange={handlePriceInputChange}
                onBlur={handlePriceBlur}
              />
              <button
                type="button"
                className={styles.priceBtn}
                onClick={() => {
                  setUseCurrentPrice(false);
                  const newPrice = Math.max(price - 10, 0);
                  setPrice(newPrice);
                  setPriceInput(newPrice.toString());
                }}
              ><RemoveIcon/></button>
              <button
                type="button"
                className={styles.priceBtn}
                onClick={() => {
                  setUseCurrentPrice(false);
                  const newPrice = price + 10;
                  setPrice(newPrice);
                  setPriceInput(newPrice.toString());
                }}
              ><AddIcon/></button>
            </div>
            <div className={styles.checkboxWrapper}>
              <input
                type="checkbox"
                id="useCurrentPrice"
                checked={useCurrentPrice}
                onChange={(e) => setUseCurrentPrice(e.target.checked)}
              />
              <label htmlFor="useCurrentPrice">자동(현재가)</label>
            </div>
          </div>

          {/* 주문총액 및 버튼 */}
          <div className={styles.total}>
            <label>주문총액</label>
            <div className={styles.totalContainer}>
              <input type="text" value={totalPrice.toLocaleString()} readOnly />
              <span>원</span>
            </div>
          </div>

          <button
            type="submit"
            className={`${styles.submit} ${orderType === "buy" ? styles.bidColor : styles.askColor}`}
          >
            {orderType === "buy" ? "매수" : "매도"}
          </button>
        </form>
      </div>
      
      <TabMenu
        tabs={tabList}
        activeTab={activeTab}
        onTabChange={(tab) => setActiveTab(tab)}
        tabTextSize="1rem"
      />
      <div className={styles.myOrderWrapper}>
        <MyOrder activeTab={activeTab} />
      </div>
    </div>
  );
};

export default StockOrder;