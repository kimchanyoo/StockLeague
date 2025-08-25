import { Client, IMessage } from "@stomp/stompjs";

export let stompClient: Client | null = null;

interface QueuedMessage {
  destination: string;
  body: any;
}

const messageQueue: QueuedMessage[] = [];

export const connectStomp = (
  accessToken: string,
  onMessage: (body: any) => void
): Promise<void> => {
  return new Promise<void>((resolve, reject) => {
    const socketUrl = process.env.NEXT_PUBLIC_SOCKET_URL;
    if (!socketUrl) {
      reject(new Error("Socket URL is not defined in environment variables."));
      return;
    }

    stompClient = new Client({
      webSocketFactory: () => new WebSocket(`${socketUrl}?access_token=${accessToken}`),
      connectHeaders: {
        Authorization: `Bearer ${accessToken}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient?.subscribe("/user/queue/notifications", (message: IMessage) => {
          try {
            const payload = JSON.parse(message.body);
            onMessage(payload);
          } catch (e) {
            //console.error("STOMP 메시지 파싱 실패:", e);
          }
        });

        flushMessageQueue();
        resolve();
      },
      onStompError: (frame) => {
        //console.error("STOMP 에러:", frame);
        reject(new Error("STOMP 연결 실패"));
      },
      onWebSocketClose: (event) => {
        console.warn("WebSocket 연결 종료:", event);
      },
      onDisconnect: () => {
        console.log("STOMP 연결 해제");
      },
    });

    stompClient.activate();
  });
};

export const disconnectStomp = () => {
  if (stompClient?.active) {
    stompClient.deactivate();
    stompClient = null;
  }
};

export const sendMessage = (destination: string, body: any) => {
  if (stompClient?.active) {
    stompClient.publish({
      destination,
      body: JSON.stringify(body),
    });
  } else {
    // 연결 전이면 큐에 저장
    messageQueue.push({ destination, body });
  }
};

const flushMessageQueue = () => {
  if (!stompClient?.active) return;

  while (messageQueue.length > 0) {
    const msg = messageQueue.shift();
    if (msg) {
      stompClient.publish({
        destination: msg.destination,
        body: JSON.stringify(msg.body),
      });
    }
  }
};
