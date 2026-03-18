import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Calendar, Download, FileText } from "lucide-react";

import { getCampaignReports, downloadCampaignReport } from "../../api/apiCampaigns";
import LoadingState from "../../components/states/LoadingState";
import EmptyState from "../../components/states/EmptyState";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";

interface Report {
  id: number;
  name: string;
  generatedAt: string;
}

const CampaignReportsPage = () => {
  const { id } = useParams();
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);

  useEffect(() => {
    const load = async () => {
      if (!id) return;

      try {
        const data = await getCampaignReports(Number(id));
        setReports(data || []);
      } catch (err) {
        console.error(err);
        setToastMessage({ type: "fail", text: "Failed to load reports" });
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  const handleDownload = async (report: Report) => {
    try {
      await downloadCampaignReport(report.id, report.name);
      setToastMessage({ type: "success", text: "Report downloaded successfully" });
    } catch (err) {
      console.error(err);
      setToastMessage({ type: "fail", text: "Failed to download report" });
    }
  };

  if (loading) return <LoadingState message="Loading reports..." />;

  return (
    <div className="p-8">

      {toastMessage &&
        (toastMessage.type === "success" ? (
          <SuccessMessage entity={toastMessage.text} />
        ) : (
          <FailMessage entity={toastMessage.text} />
        ))}

      <h1 className="text-2xl font-bold mb-6">Campaign Reports</h1>

      {reports.length === 0 ? (
        <EmptyState
          icon={<FileText size={48} />}
          title="No reports found"
          description="This campaign has no reports generated yet."
        />
      ) : (
        <div className="grid gap-4">
          {reports.map((r) => (
            <div
              key={r.id}
              className="bg-white p-4 rounded-xl shadow-sm border flex justify-between items-center"
            >
              <div className="flex items-center gap-4">
                <FileText className="w-8 h-8 text-indigo-600" />

                <div>
                  <p className="text-lg font-semibold">{r.name}</p>
                  <div className="flex items-center gap-1 text-gray-500 text-sm">
                    <Calendar className="w-4 h-4" />
                    {new Date(r.generatedAt).toLocaleString()}
                  </div>
                </div>
              </div>

              <button
                onClick={() => handleDownload(r)}
                className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700"
              >
                <Download className="w-5 h-5" />
                Download
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CampaignReportsPage;
