'use client';

import styles from '@/app/styles/components/Comment.module.css';
import { useState } from 'react';

type CommentType = {
  id: number;
  parentId: number | null;
  nickname: string;
  content: string;
  createdAt: string;
};

type CommentProps = {
  comments?: CommentType[];
};

export default function Comment({ comments = [] }: CommentProps) {
  const [likedMap, setLikedMap] = useState<{ [key: number]: boolean }>({});
  const [likesMap, setLikesMap] = useState<{ [key: number]: number }>({});
  const [replyToggleMap, setReplyToggleMap] = useState<{ [key: number]: boolean }>({});
  const [reportModalId, setReportModalId] = useState<number | null>(null);

  const toggleLike = (commentId: number) => {
    setLikedMap((prev) => ({
      ...prev,
      [commentId]: !prev[commentId],
    }));

    setLikesMap((prev) => ({
      ...prev,
      [commentId]: prev[commentId] ? prev[commentId] - 1 : (prev[commentId] || 0) + 1,
    }));
  };

  const toggleReply = (commentId: number) => {
    setReplyToggleMap((prev) => ({
      ...prev,
      [commentId]: !prev[commentId],
    }));
  };

  const openReportModal = (commentId: number) => {
    setReportModalId(commentId);
  };

  const closeReportModal = () => {
    setReportModalId(null);
  };

  const getReplies = (parentId: number) =>
    comments.filter((comment) => comment.parentId === parentId);

  const renderReplies = (parentId: number) => {
    const replies = getReplies(parentId);

    return (
      replyToggleMap[parentId] &&
      replies.map((reply) => (
        <div key={`reply-${parentId}-${reply.id}`} className={styles.replySection}>
          <div className={styles.comment}>
            <div className={styles.headerRow}>
              <div className={styles.leftSide}>
                <span className={styles.nickname}>{reply.nickname}</span>
                <span className={styles.dot}>|</span>
                <span className={styles.time}>{reply.createdAt}</span>
                <button className={styles.actionBtn} onClick={() => toggleLike(reply.id)}>
                  {likedMap[reply.id] ? '좋아요 취소' : '좋아요'} ({likesMap[reply.id] || 0})
                </button>
              </div>
              <div className={styles.rightSide}>
                <button className={styles.reportBtn} onClick={() => openReportModal(reply.id)}>
                  신고
                </button>
              </div>
            </div>
            <p className={styles.commentContent}>{reply.content}</p>
          </div>
        </div>
      ))
    );
  };

  const topLevelComments = comments.filter((comment) => comment.parentId === null);

  return (
    <div className={styles.container}>
      {topLevelComments.length === 0 ? (
        <p className={styles.noComments}>댓글이 없습니다.</p>
      ) : (
        topLevelComments.map((comment) => {
          const replyCount = getReplies(comment.id).length;
          return (
            <div key={`comment-${comment.id}`} className={styles.comment}>
              <div className={styles.headerRow}>
                <div className={styles.leftSide}>
                  <span className={styles.nickname}>{comment.nickname}</span>
                  <span className={styles.dot}>|</span>
                  <span className={styles.time}>{comment.createdAt}</span>
                  <button className={styles.actionBtn} onClick={() => toggleLike(comment.id)}>
                    {likedMap[comment.id] ? '좋아요 취소' : '좋아요'} ({likesMap[comment.id] || 0})
                  </button>
                  <button className={styles.actionBtn} onClick={() => toggleReply(comment.id)}>
                    {replyToggleMap[comment.id] ? '대댓글 닫기' : `대댓글 (${replyCount})`}
                  </button>
                </div>
                <div className={styles.rightSide}>
                  <button className={styles.reportBtn} onClick={() => openReportModal(comment.id)}>
                    신고
                  </button>
                </div>
              </div>

              <p className={styles.commentContent}>{comment.content}</p>

              {replyToggleMap[comment.id] && (
                <div className={styles.replySection}>
                  <textarea className={styles.replyInput} placeholder="대댓글을 작성하세요." />
                  <button className={styles.submitReply}>작성</button>
                </div>
              )}

              {renderReplies(comment.id)}

              {reportModalId === comment.id && (
                <div className={styles.reportModal}>
                  <div className={styles.modalContent}>
                    <p>이 댓글을 신고하시겠습니까?</p>
                    <div className={styles.modalButtons}>
                      <button onClick={closeReportModal}>닫기</button>
                      <button className={styles.reportConfirm}>신고</button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          );
        })
      )}
    </div>
  );
}
