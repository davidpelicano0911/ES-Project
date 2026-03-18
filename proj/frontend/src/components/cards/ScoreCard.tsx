import type React from "react";

interface ScoreCardProps {
  score: number;
  label: string;
  lastUpdated: string;
}

const ScoreCard: React.FC<ScoreCardProps> = ({ score, label, lastUpdated }) => {
  return (
    <div className="bg-white rounded-xl shadow-sm p-6 flex items-center justify-between">
      <div>
        <p className="text-gray-500 text-sm">Lead Score</p>
        <div className="flex items-center gap-3 mt-1">
          <span className="text-3xl font-bold text-gray-900">{score}</span>
          <span className="text-sm text-red-600 font-medium">{label}</span>
        </div>
        <p className="text-xs text-gray-400 mt-1">Last updated: {lastUpdated}</p>
      </div>
      <div className="flex-1 ml-8">
        <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
          <div
            className="h-2 bg-red-500"
            style={{ width: `${Math.max(0, Math.min(100, score))}%` }}
          />
        </div>
        <div className="flex justify-between text-xs text-gray-400 mt-1">
          <span>Cold (0–33)</span>
          <span>Warm (34–66)</span>
          <span>Hot (67–100)</span>
        </div>
      </div>
    </div>
  );
};

export default ScoreCard;
