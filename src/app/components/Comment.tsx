'use client';

import styles from '@/app/styles/components/Comment.module.css';
import { useState, useEffect } from 'react';
import { 
  Comment as CommentType, 
  getCommentsByTicker, 
  getRepliesByCommentId, 
  Reply, 
  postReply, 
  deleteComment, 
  patchComment, 
  patchReply, 
  deleteReply,
  reportComment,
  ReportPayload
} 
from "@/lib/api/comment";

type CommentProps = {
  ticker: string;
  refreshTrigger: number;
};

const Comment = ({ ticker, refreshTrigger }: CommentProps) => {
  const [comments, setComments] = useState<CommentType[]>([]);
  const [likedMap, setLikedMap] = useState<{ [key: number]: boolean }>({});
  const [likesMap, setLikesMap] = useState<{ [key: number]: number }>({});
  const [replyToggleMap, setReplyToggleMap] = useState<{ [key: number]: boolean }>({});
  const [reportModalId, setReportModalId] = useState<number | null>(null);
  const [repliesMap, setRepliesMap] = useState<{ [key: number]: Reply[] }>({}); 
  const [replyInputMap, setReplyInputMap] = useState<{ [key: number]: string }>({});
  const [reportReason, setReportReason] = useState('');
  const [reportContent, setReportContent] = useState('');
  
  useEffect(() => {
    getCommentsByTicker(ticker)
      .then(res => {
        if (res.success) {
          setComments(res.comments); 
          // 좋아요 수, 좋아요 여부 초기화 (옵션)
          const initialLikesMap: { [key: number]: number } = {};
          const initialLikedMap: { [key: number]: boolean } = {};
          res.comments.forEach(comment => {
            initialLikesMap[comment.commentId] = comment.likeCount;
            initialLikedMap[comment.commentId] = comment.isLiked;
          });
          setLikesMap(initialLikesMap);
          setLikedMap(initialLikedMap);
        }
      })
      .catch(err => {
        console.error("댓글 목록 조회 실패", err);
      });
  }, [ticker, refreshTrigger]);

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

  const toggleReply = async (commentId: number) => {
    setReplyToggleMap((prev) => {
      const newVal = !prev[commentId];
      // 대댓글 토글이 true가 되면 대댓글 API 호출
      if (newVal && !repliesMap[commentId]) {
        getRepliesByCommentId(commentId)
          .then((res) => {
            if (res.success) {
              setRepliesMap((prevReplies) => ({
                ...prevReplies,
                [commentId]: res.data,
              }));
            }
          })
          .catch((err) => {
            console.error("대댓글 조회 실패", err);
          });
      }
      return {
        ...prev,
        [commentId]: newVal,
      };
    });
  };

  const submitReply = async (commentId: number) => {
    const content = replyInputMap[commentId];

    if (!content || content.trim() === '') {
      alert('대댓글 내용을 입력하세요.');
      return;
    }
    
    try {
      const response = await postReply(ticker, commentId, content );
      if (response.success) {
        // 대댓글 목록 최신화: 새로 작성한 대댓글을 repliesMap에 추가
        const newReply = {
          replyId: response.replyId,
          commentId,
          userNickname: response.nickname,
          content,
          createdAt: response.createdAt,
          isAuthor: true,
          likeCount: 0,
          isLiked: false,
        };
        setRepliesMap((prev) => ({
          ...prev,
          [commentId]: prev[commentId] ? [newReply, ...prev[commentId]] : [newReply],
        }));
        // 입력창 비우기
        setReplyInputMap((prev) => ({
          ...prev,
          [commentId]: '',
        }));
      } else {
        alert('대댓글 작성에 실패했습니다.');
      }
    } catch (error) {
      console.error(error);
      alert('대댓글 작성 중 오류가 발생했습니다.');
    }
  };

  const openReportModal = (commentId: number) => {
    setReportModalId(commentId);
  };

  const closeReportModal = () => {
    setReportReason('');   // 초기화
    setReportContent(''); // 초기화
    setReportModalId(null);
  };

  const handleReplyInputChange = (commentId: number, value: string) => {
    setReplyInputMap((prev) => ({
      ...prev,
      [commentId]: value,
    }));
  }

  // 댓글 수정 삭제
  const handleEditComment = async (commentId: number, oldContent: string) => {
    const newContent = prompt('댓글을 수정하세요:', oldContent);
    if (newContent === null) return; // 취소
    if (newContent.trim() === '') {
      alert('내용을 입력하세요.');
      return;
    }
    try {
      const res = await patchComment(commentId, { content: newContent.trim() });
      if (res.success) {
        setComments((prev) =>
          prev.map((c) => (c.commentId === commentId ? { ...c, content: newContent.trim() } : c))
        );
        alert('댓글이 수정되었습니다.');
      } else {
        alert(res.message || '댓글 수정에 실패했습니다.');
      }
    } catch (error) {
      console.error(error);
      alert('댓글 수정 중 오류가 발생했습니다.');
    }
  };
  const handleDeleteComment = async (commentId: number) => {
    if (!confirm('댓글을 삭제하시겠습니까?')) return;
    try {
      const res = await deleteComment(commentId);
      if (res.success) {
        setComments((prev) => prev.filter((c) => c.commentId !== commentId));
        alert('댓글이 삭제되었습니다.');
      } else {
        alert(res.message || '댓글 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error(error);
      alert('댓글 삭제 중 오류가 발생했습니다.');
    }
  };

  // 대댓글 수정 삭제
  const handleEditReply = async (
    replyId: number,
    parentCommentId: number,
    oldContent: string
  ) => {
    const newContent = prompt('대댓글을 수정하세요:', oldContent);
    if (newContent === null) return;
    if (newContent.trim() === '') {
      alert('내용을 입력하세요.');
      return;
    }
    try {
      const res = await patchReply(replyId, { content: newContent.trim() });
      if (res.success) {
        setRepliesMap((prev) => ({
          ...prev,
          [parentCommentId]: prev[parentCommentId].map((r) =>
            r.replyId === replyId ? { ...r, content: newContent.trim() } : r
          ),
        }));
        alert('대댓글이 수정되었습니다.');
      } else {
        alert(res.message || '대댓글 수정에 실패했습니다.');
      }
    } catch (error) {
      console.error(error);
      alert('대댓글 수정 중 오류가 발생했습니다.');
    }
  };
  const handleDeleteReply = async (replyId: number, parentCommentId: number) => {
    if (!confirm('대댓글을 삭제하시겠습니까?')) return;
    try {
      const res = await deleteReply(replyId); // API가 댓글 삭제와 같은 경로라면 재사용 가능
      if (res.success) {
        setRepliesMap((prev) => ({
          ...prev,
          [parentCommentId]: prev[parentCommentId].filter((r) => r.replyId !== replyId),
        }));
        alert('대댓글이 삭제되었습니다.');
      } else {
        alert(res.message || '대댓글 삭제에 실패했습니다.');
      }
    } catch (error) {
      console.error(error);
      alert('대댓글 삭제 중 오류가 발생했습니다.');
    }
  };

const handleReport = async (commentId: number) => {
  if (!reportReason) {
    alert('신고 사유를 선택해주세요.');
    return;
  }

  try {
    await reportComment(commentId, {
      reason: reportReason as ReportPayload["reason"],
      additionalInfo: reportContent,
    });
    alert('신고가 접수되었습니다.');
    closeReportModal();
    setReportReason('');
    setReportContent('');
  } catch (error) {
    alert('신고에 실패했습니다.');
    console.error(error);
  }
};

  const renderReplies = (parentId: number) => {
    const replies = repliesMap[parentId] || [];

    return (
      replies.map((reply) => (
        <div key={`reply-${parentId}-${reply.replyId}`} className={styles.replySection}>
          <div className={styles.comment}>
            <div className={styles.headerRow}>
              <div className={styles.leftSide}>
                <span className={styles.nickname}>{reply.userNickname}</span>
                <span className={styles.dot}>|</span>
                <span className={styles.time}>{reply.createdAt}</span>
                <button className={styles.actionBtn} onClick={() => toggleLike(reply.replyId)}>
                  {likedMap[reply.replyId] ? '좋아요 취소' : '좋아요'} ({likesMap[reply.replyId] || 0})
                </button>
                {reply.isAuthor && (
                  <>
                    <button
                      className={styles.actionBtn}
                      onClick={() => handleEditReply(reply.replyId, parentId, reply.content)}
                    >
                      수정
                    </button>
                    <button
                      className={styles.actionBtn}
                      onClick={() => handleDeleteReply(reply.replyId, parentId)}
                    >
                      삭제
                    </button>
                  </>
                )}
              </div>
              <div className={styles.rightSide}>
                <button className={styles.reportBtn} onClick={() => openReportModal(reply.replyId)}>
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

  return (
    <div className={styles.container}>
      {comments.length === 0 ? (
        <p className={styles.noComments}>댓글이 없습니다.</p>
      ) : (
        comments.map((comment) => {
          const replyCount = comment.replyCount || 0;
          return (
            <div key={`comment-${comment.commentId}`} className={styles.comment}>
              <div className={styles.headerRow}>
                <div className={styles.leftSide}>
                  <span className={styles.nickname}>{comment.userNickname}</span>
                  <span className={styles.dot}>|</span>
                  <span className={styles.time}>{comment.createdAt}</span>
                  <button className={styles.actionBtn} onClick={() => toggleLike(comment.commentId)}>
                    {likedMap[comment.commentId] ? '좋아요 취소' : '좋아요'} ({likesMap[comment.commentId] || 0})
                  </button>
                  <button className={styles.actionBtn} onClick={() => toggleReply(comment.commentId)}>
                    {replyToggleMap[comment.commentId] ? '대댓글 닫기' : `대댓글 (${replyCount})`}
                  </button>
                  {/* 내 댓글이면 수정/삭제 버튼 보여주기 */}
                  {comment.isAuthor && (
                    <>
                      <button
                        className={styles.actionBtn}
                        onClick={() => handleEditComment(comment.commentId, comment.content)}
                      >
                        수정
                      </button>
                      <button
                        className={styles.actionBtn}
                        onClick={() => handleDeleteComment(comment.commentId)}
                      >
                        삭제
                      </button>
                    </>
                  )}
                </div>
                <div className={styles.rightSide}>
                  <button className={styles.reportBtn} onClick={() => openReportModal(comment.commentId)}>
                    신고
                  </button>
                </div>
              </div>

              <p className={styles.commentContent}>{comment.content}</p>

              {replyToggleMap[comment.commentId] && (
                <div className={styles.replySection}>
                  <textarea 
                    className={styles.replyInput} 
                    placeholder="대댓글을 작성하세요." 
                    value={replyInputMap[comment.commentId] || ''} 
                    onChange={(e) => handleReplyInputChange(comment.commentId, e.target.value)}/>
                  <button className={styles.submitReply} onClick={() => submitReply(comment.commentId)}>작성</button>
                </div>
              )}

              {replyToggleMap[comment.commentId] && renderReplies(comment.commentId)}

              {reportModalId === comment.commentId && (
                <div className={styles.reportModal}>
                  <div className={styles.modalContent}>
                    <p>이 댓글을 신고하시겠습니까?</p>
                    {/* 신고 사유 선택 */}
                    <div className={styles.reasonGroup}>
                      <label htmlFor="reason">신고 사유</label>
                      <select
                        id="reason"
                        value={reportReason}
                        onChange={(e) => setReportReason(e.target.value)}
                      >
                        <option value="">-- 선택해주세요 --</option>
                        <option value="INSULT">욕설 및 비방</option>
                        <option value="SPAM">광고 / 도배성 내용</option>
                        <option value="PERSONAL_INFORMATION">개인정보 노출</option>
                        <option value="SEXUAL">선정적인 내용</option>
                        <option value="OTHER">기타</option>
                      </select>
                    </div>

                    {/* 신고 상세 내용 */}
                    <div className={styles.contentGroup}>
                      <label htmlFor="content">신고 내용</label>
                      <textarea
                        id="content"
                        value={reportContent}
                        onChange={(e) => setReportContent(e.target.value)}
                        placeholder="신고 사유에 대한 상세 설명을 입력해주세요."
                      />
                    </div>
                    <div className={styles.modalButtons}>
                      <button onClick={closeReportModal}>닫기</button>
                      <button className={styles.reportConfirm} onClick={() => handleReport(comment.commentId)}>신고</button>
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
};

export default Comment;