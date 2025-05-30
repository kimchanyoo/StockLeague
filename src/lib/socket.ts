import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export let stompClient: Client | null = null;

export const connectStomp = (accessToken: string, onMessage: (body: any) => void) => {

  return new Promise<void>((resolve, reject) => {
    const socketUrl = process.env.NEXT_PUBLIC_SOCKET_URL;
    if (!socketUrl) {
      reject(new Error("Socket URL is not defined in environment variables."));
      return;
    }
    stompClient = new Client({
      webSocketFactory: () => {
        const sock = new SockJS(socketUrl) as any;
        return sock;
      },
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      debug: (str) => {
        console.log("[STOMP DEBUG]", str);
      },
      onConnect: () => {
        console.log("✅ STOMP 연결 성공");

        stompClient?.subscribe("/user/queue/notifications", (message) => {
           console.log("subscribe 콜백 진입");
          try {
            const payload = JSON.parse(message.body);
            console.log("🔔 받은 메시지:", payload);
            onMessage(payload);
          } catch (e) {
            console.error("메시지 파싱 실패:", e);
          }
        });
        
        flushMessageQueue();
        resolve();
      },
      onStompError: (frame) => {
        console.error("❌ STOMP 에러:", frame);
        reject(frame);
      },
      onWebSocketClose: (event) => {
        console.log("WebSocket 연결 종료:", event);
      },
      onDisconnect: () => {
        console.log("STOMP 연결 해제");
      },
    });

    stompClient.activate();
  });
};

export const disconnectStomp = () => {
  if (stompClient && stompClient.active) {
    stompClient.deactivate();
  }
};

const messageQueue: Array<{destination: string; body: any}> = [];

export const sendMessage = (destination: string, body: any) => {
  if (stompClient && stompClient.active) {
    stompClient.publish({
      destination,
      body: JSON.stringify(body),
    });
  } else {
    console.warn("STOMP 연결되지 않음 - 메시지를 큐에 저장합니다.");
    messageQueue.push({ destination, body });
  }
};

// 연결 완료 시 큐 비우기
const flushMessageQueue = () => {
  if (stompClient && stompClient.active) {
    while (messageQueue.length > 0) {
      const msg = messageQueue.shift();
      if (msg) {
        sendMessage(msg.destination, msg.body);
      }
    }
  }
};
