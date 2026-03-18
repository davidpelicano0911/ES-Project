import api from "./apiConfig";
import type { Post, PostDTO } from "../types/post";

export const getPosts = async (): Promise<Post[]> => {
  const res = await api.get<Post[]>("/posts");
  console.log("API getPosts response:", res);
  return res.data;
};

export const getPostById = async (
  id: number
): Promise<Post> => {
  const res = await api.get<Post>(`/posts/${id}`);
  return res.data;
};

export const createPost = async (
  post: PostDTO
): Promise<Post> => {
  const res = await api.post<Post>("/posts", post);
  return res.data;
};

export const updatePost = async (
  id: number,
  updated: Partial<Post>
): Promise<Post> => {
  const res = await api.put<Post>(`/posts/${id}`, updated);
  return res.data;
};

export const deletePost = async (id: number): Promise<void> => {
  await api.delete(`/posts/${id}`);
};

export const testPost = async (id: number): Promise<void> => {
  await api.post(`/posts/${id}/test`);
};

export const getImage = async (
  filename: string
): Promise<string> => {
  const response = await api.get(`/posts/images/${filename}`, { responseType: "blob" });

  return URL.createObjectURL(response.data);
};


export const getPostStats = async (id: number) => {
  const res = await api.get(`/posts/${id}/stats`);
  return res.data;
}