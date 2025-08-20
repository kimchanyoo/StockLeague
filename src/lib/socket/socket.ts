import { Client, IMessage, StompConfig } from "@stomp/stompjs";

export let stompClient: Client | null = null;

const messageQueue: Array<{ destination: string; body: any }> = [];

// 연결 잠시 비활성화 플래그
export let STOMP_DISABLED = true;

export const connectStomp = (
  accessToken: string,
  onMessage: (body: any) => void
): Promise<void> => {
  if (STOMP_DISABLED) {
    // STOMP 비활성화 시 그냥 resolve
    return Promise.resolve();
  }

  return new Promise<void>((resolve, reject) => {
    const socketUrl = process.env.NEXT_PUBLIC_SOCKET_URL;
    if (!socketUrl) {
      reject(new Error("Socket URL is not defined in environment variables."));
      return;
    }
    const config: StompConfig = {
      webSocketFactory: () => new WebSocket(socketUrl) as any,
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      //debug: (str) => console.log("[STOMP DEBUG]", str),
      onConnect: (frame) => {
       // console.log("✅ STOMP 연결 성공:", frame);

        if (!stompClient) {
          reject(new Error("stompClient is null onConnect"));
          return;
        }

        stompClient.subscribe("/user/queue/notifications", (message: IMessage) => {
        //  console.log("subscribe 콜백 진입");
          try {
            const payload = JSON.parse(message.body);
           // console.log("🔔 받은 메시지:", payload);
            onMessage(payload);
          } catch (e) {
           // console.error("메시지 파싱 실패:", e);
          }
        });

        flushMessageQueue();
        resolve();
      },
      onStompError: (frame) => {
        //console.error("❌ STOMP 에러:", frame);
        reject(frame);
      },
      onWebSocketClose: (event) => {
      //  console.log("WebSocket 연결 종료:", event);
      },
      onDisconnect: () => {
       // console.log("STOMP 연결 해제");
      },
    };

    stompClient = new Client(config);

    stompClient.activate();
  });
};

export const disconnectStomp = () => {
  if (!STOMP_DISABLED && stompClient && stompClient.active) {
    stompClient.deactivate();
  }
};

export const sendMessage = (destination: string, body: any) => {
  if (STOMP_DISABLED) return; 
  if (stompClient && stompClient.active) {
    stompClient.publish({
      destination,
      body: JSON.stringify(body),
    });
  } else {
   // console.warn("STOMP 연결되지 않음 - 메시지를 큐에 저장합니다.");
    messageQueue.push({ destination, body });
  }
};

const flushMessageQueue = () => {
  if (STOMP_DISABLED) return; 
  if (stompClient && stompClient.active) {
    while (messageQueue.length > 0) {
      const msg = messageQueue.shift();
      if (msg) {
        sendMessage(msg.destination, msg.body);
      }
    }
  }
};
