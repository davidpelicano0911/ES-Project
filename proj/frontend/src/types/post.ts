export type PostStatus = 'SCHEDULED' | 'PUBLISHED' | 'FAILED' | 'CANCELLED';

export interface PostDTO {
  id: number;
  name: string;
  description: string;
  scheduled_date: Date;
  image: File;
  platforms: string[];
}


export interface Post {
  id: number;
  name: string;
  description: string;
  scheduled_date: string;
  file_path: string | null;
  platforms: PostPlatform[];
}

export interface PostPlatform {
  id?: number; 
  post?: Post; 
  platformPostId?: string | null;
  platformType: string;
  status: PostStatus;
  numberLikes?: number | null;
  numberShares?: number | null;
  numberComments?: number | null;
  numberReachs?: number | null;
  postUrl?: string | null;
  performanceScore?: number | null;
}


export interface FacebookPlatform extends PostPlatform {
  formsId?: string | null;
}

