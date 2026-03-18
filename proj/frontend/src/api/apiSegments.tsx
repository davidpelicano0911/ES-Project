import api from "./apiConfig";
import type { Segment } from "../types/segment";

export const getSegments = async (): Promise<Segment[]> => {
  const response = await api.get<Segment[]>("/segments/");
  return response.data;
};
