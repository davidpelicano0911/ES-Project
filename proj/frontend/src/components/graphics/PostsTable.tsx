import { useEffect, useState } from "react";
import { Trophy, Calendar, TrendingUp, Facebook } from "lucide-react";
import { getCampaignMaterials } from "../../api/apiCampaigns"; 


interface PostsTableProps {
  campaignId?: number;
}

const PostsTable = ({ campaignId }: PostsTableProps) => {
  const [topPosts, setTopPosts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getCampaignMaterials(campaignId || 0)
      .then((data) => {
        const postsOnly = data.filter((item: any) => 
            item.type === 'POST' || item.dtype === 'POST'
        );
        const postsWithScore = postsOnly.map((post) => {
          const validPlatforms = post.platforms.filter((p: any) => p.performanceScore != null);
          const avgScore = validPlatforms.length > 0
            ? validPlatforms.reduce((acc: number, curr: any) => acc + (curr.performanceScore || 0), 0) / validPlatforms.length
            : 0;
          return { ...post, avgScore };
        });

        const sorted = postsWithScore
          .sort((a, b) => b.avgScore - a.avgScore)
          .slice(0, 5);

        setTopPosts(sorted);
      })
      .catch((err) => console.error("Erro", err))
      .finally(() => setLoading(false));
  }, [campaignId]);

  if (loading) return <div className="p-6 text-center text-gray-400 text-sm">Loading rankings...</div>;

  return (
    <div className="w-full h-full flex flex-col">
      <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
        <h3 className="font-bold text-gray-800">Top Performing Posts</h3>
        <span className="text-xs text-gray-400">Based on Performance Score of the Social Posts</span>
      </div>

      <div className="flex-1 overflow-auto">
        <table className="w-full text-left border-collapse">
          <thead className="bg-gray-50 sticky top-0">
            <tr>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider w-16">Rank</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Post Details</th>
              <th className="px-6 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider text-right">Score</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {topPosts.length === 0 ? (
               <tr><td colSpan={3} className="p-6 text-center text-sm text-gray-500">No posts with data yet.</td></tr>
            ) : (
              topPosts.map((post, index) => (
                <tr key={post.id} className="hover:bg-blue-50/30 transition-colors group">
                  
                  <td className="px-6 py-4">
                    <div className={`
                      flex items-center justify-center w-8 h-8 rounded-full font-bold text-sm
                      ${index === 0 ? "bg-yellow-100 text-yellow-700 ring-4 ring-yellow-50" : 
                        index === 1 ? "bg-gray-100 text-gray-700" : 
                        index === 2 ? "bg-orange-100 text-orange-800" : "text-gray-500"}
                    `}>
                      {index === 0 ? <Trophy size={14} /> : index + 1}
                    </div>
                  </td>

                  <td className="px-6 py-4">
                    <div className="flex flex-col">
                      <span className="font-medium text-gray-800 truncate max-w-[200px]" title={post.name}>
                        {post.name || "Untitled Post"}
                      </span>
                      <div className="flex items-center gap-2 mt-1 text-xs text-gray-400">
                        <Calendar size={10} />
                        <span>{new Date(post.scheduled_date || Date.now()).toLocaleDateString()}</span>
                        
                        <span>•</span>
                        
                        <div className="flex items-center gap-1 text-blue-600 bg-blue-50 px-1.5 py-0.5 rounded text-[10px] font-semibold border border-blue-100">
                           <Facebook size={10} fill="currentColor" />
                           <span>Facebook</span>
                        </div>

                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-4 text-right">
                    <div className="flex flex-col items-end">
                      <span className={`
                        inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-bold
                        ${post.avgScore >= 0.7 ? "bg-green-100 text-green-700" : 
                          post.avgScore >= 0.4 ? "bg-yellow-100 text-yellow-700" : "bg-red-100 text-red-700"}
                      `}>
                        <TrendingUp size={12} />
                        {post.avgScore.toFixed(2)}
                      </span>
                      <span className="text-[10px] text-gray-400 mt-1">Avg. Impact</span>
                    </div>
                  </td>

                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default PostsTable;