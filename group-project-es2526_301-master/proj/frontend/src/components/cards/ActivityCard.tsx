import type React from "react";
import { Mail, FileText, Clock, MessageSquare, Globe } from "lucide-react";
import type { Activity } from "../../types/activity";

interface ActivityCardProps {
  activity: Activity;
  timeAgo: (iso: string) => string;
}

const ActivityCard: React.FC<ActivityCardProps> = ({ activity, timeAgo }) => {
  return (
    <div className="flex items-start gap-4 py-4 border-b last:border-0">
      <div className="w-10 h-10 rounded-full flex items-center justify-center bg-gray-100">
  {activity.type === "email" && <Mail className="h-5 w-5 text-green-500" />}
  {activity.type === "link" && <FileText className="h-5 w-5 text-blue-500" />}
  {activity.type === "website" && <Clock className="h-5 w-5 text-orange-500" />}
  {activity.type === "form" && <MessageSquare className="h-5 w-5 text-purple-500" />}
  {activity.type === "landing" && <Globe className="h-5 w-5 text-indigo-500" />}
      </div>
      <div className="flex-1">
        <p className="font-medium text-gray-900 text-sm">{activity.title}</p>
        <p className="text-gray-500 text-xs mt-1">{activity.description}</p>
      </div>
      <p className="text-xs text-gray-400 whitespace-nowrap">{timeAgo(activity.timestamp)}</p>
    </div>
  );
};

export default ActivityCard;
