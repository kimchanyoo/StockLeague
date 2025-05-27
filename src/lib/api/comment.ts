import axiosInstance from "./axiosInstance";

// 댓글 목록
export interface Comment {
  commentId: number;
  userNickname: string;
  content: string;
  createdAt: string;
  isAuthor: boolean;
  likeCount: number;
  isLiked: boolean;
  replyCount: number;
}

// 댓글 목록
export interface CommentListResponse {
  success: boolean;
  comments: Comment[];
  page: number;
  size: number;
  totalCount: number;
}

// 대댓글 목록
export interface Reply {
  replyId: number;
  commentId: number;
  userNickname: string;
  content: string;
  createdAt: string;
  isAuthor: boolean;
  likeCount: number;
  isLiked: boolean;
}

// 대댓글 목록
export interface ReplyListResponse {
  success: boolean;
  data: Reply[];
}

// 댓글 수정 요청 
export interface CommentUpdateRequest {
  content: string;
}

// 댓글 수정 
export interface CommentUpdateResponse {
  success: boolean;
  message: string;
}

// 댓글 삭제 
export interface CommentDeleteResponse {
  success: boolean;
  message: string;
}

// 대댓글 목록
export interface ReplyListResponse {
  success: boolean;
  data: Reply[];
}

// 대댓글 수정 요청
export interface ReplyUpdateRequest {
  content: string;
}

// 대댓글 수정 응답
export interface ReplyUpdateResponse {
  success: boolean;
  message: string;
}

// 대댓글 삭제 응답
export interface ReplyDeleteResponse {
  success: boolean;
  message: string;
}
type Reason = "INSULT" | "SPAM" | "PERSONAL_INFORMATION" | "SEXUAL" | "OTHER";
// 댓글 신고
export interface ReportPayload {
  reason: Reason;
  additionalInfo: string;
}

// 신고 목록
export type ReportStatus = 'WAITING' | 'RESOLVED' | null;
export interface Report {
  commentId: number;
  authorNickname: string;
  reportCount: number;
  warningCount: number;
  status: ReportStatus;
}
export interface ReportListResponse {
  success: boolean;
  reports: Report[];
  page: number;
  size: number;
  totalCount: number;
  totalPage: number;
}

// 신고 상세
export interface SingleReport {
  reporterNickname: string;
  reason: string;
  additionalInfo: string;
  reportedAt: string;
}

export interface Warning {
  warningAt: string;
  reason: string;
  commentId: number;
  adminNickname: string;
}

export type ActionTaken = 'NONE' | 'REJECTED' | 'COMMENT_DELETED_AND_WARNING' | 'COMMENT_DELETED' | 'BANNED';
export interface ReportDetail {
  success: boolean;
  message: string;
  commentId: number;
  commentAuthorNickname: string;
  commentCreatedAt: string;
  stockName: string;
  commentContent: string;
  commentAuthorId: number;
  warningCount: number;
  accountStatus: boolean;
  AdminNickname: string;
  actionTaken: ActionTaken;
  reports: SingleReport[];
  warnings: Warning[];
}

// ─────────────────────────────
// 댓글 API
// ─────────────────────────────

export const getCommentsByTicker = async ( ticker: string, page: number = 1, size: number = 10 ): Promise<CommentListResponse> => {
  const res = await axiosInstance.get(`/api/v1/stocks/${ticker}/comments`, {
    params: { page, size },
  });
  return res.data;
};

export const postComment = async (ticker: string, content: string ) => {
  const res = await axiosInstance.post(`/api/v1/stocks/${ticker}/comment`, {
    content,
  });
  return res.data;
};

export const patchComment = async ( commentId: number, data: CommentUpdateRequest ): Promise<CommentUpdateResponse> => {
  const res = await axiosInstance.patch(`/api/v1/comments/${commentId}`, data);
  return res.data;
};

export const deleteComment = async ( commentId: number ): Promise<CommentDeleteResponse> => {
  const res = await axiosInstance.delete(`/api/v1/comments/${commentId}`);
  return res.data;
};

// ─────────────────────────────
// 대댓글 API
// ─────────────────────────────

export const getRepliesByCommentId = async ( commentId: number ): Promise<ReplyListResponse> => {
  const res = await axiosInstance.get(`/api/v1/comments/${commentId}/replies`);
  return res.data;
};

export const postReply = async ( ticker: string, commentId: number, content: string ) => {
  const res = await axiosInstance.post(`/api/v1/${ticker}/comments/${commentId}/replies`, { 
    content 
  });
  return res.data;
};

export const patchReply = async (replyId: number, data: ReplyUpdateRequest): Promise<ReplyUpdateResponse> => {
  const res = await axiosInstance.patch(`/api/v1/replies/${replyId}`, data);
  return res.data;
};

export const deleteReply = async (replyId: number): Promise<ReplyDeleteResponse> => {
  const res = await axiosInstance.delete(`/api/v1/replies/${replyId}`);
  return res.data;
};

// ─────────────────────────────
// 신고 API
// ─────────────────────────────

export const reportComment = async (commentId: number, payload: ReportPayload) => {
  const res = await axiosInstance.post(`/api/v1/reports/${commentId}`, payload);
  return res.data;
};

export const fetchReports = async ( page = 1, size = 10, status?: ReportStatus | null ): Promise<ReportListResponse> => {
  const params: any = { page, size };
  if (status !== null && status !== undefined) {
    params.status = status;
  }
  const res = await axiosInstance.get<ReportListResponse>('/api/v1/admin/reports', {
    params
  });
  return res.data;
};

export const fetchReportDetail = async (commentId: number): Promise<ReportDetail> => {
  const res = await axiosInstance.get(`/api/v1/admin/reports/${commentId}`);
  return res.data;
};

export const forceDeleteComment = async (commentId: number) => {
  const res = await axiosInstance.patch(`/api/v1/admin/comments/${commentId}/delete`);
  return res.data;
};

export const deleteCommentWithWarning = async (commentId: number, reason: string) => {
  const res = await axiosInstance.post(`/api/v1/admin/comments/${commentId}/delete-with-warning`, {
    reason
  });
  return res.data;
};

export const rejectReport = async (commentId: number) => {
  const res = await axiosInstance.patch(`/api/v1/admin/reports/${commentId}/reject`);
  return res.data;
};

export const banUser = async (userId: number, reason: string) => {
  const res = await axiosInstance.patch(`/api/v1/admin/users/${userId}`, { reason });
  return res.data;
};