export interface PostDTO {
  id: number;
  name: string;
  description: string;
  scheduled_date: Date;
  imageBase64: string;
  platforms: string[];
}
