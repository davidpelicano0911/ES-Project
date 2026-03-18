import {
  Mail,
  Layout,
  ClipboardList,
  Megaphone,
  FileText
} from "lucide-react";
import React from "react";

export function getTemplateIcon(typeOrName: string): React.ReactElement {
  if (!typeOrName) return <FileText className="w-6 h-6 text-gray-500" />;

  const key = typeOrName.toUpperCase();

  if (key.includes("EMAIL"))
    return <Mail className="w-6 h-6 text-blue-600" />;

  if (key.includes("LANDING_PAGE"))
    return <Layout className="w-6 h-6 text-green-600" />;

  if (key.includes("FORM"))
    return <ClipboardList className="w-6 h-6 text-purple-600" />;

  if (key.includes("POST"))
    return <Megaphone className="w-6 h-6 text-pink-600" />;

  return <FileText className="w-6 h-6 text-gray-500" />;
}