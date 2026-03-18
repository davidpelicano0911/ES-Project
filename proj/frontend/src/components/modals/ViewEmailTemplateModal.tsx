import { useState } from "react";
import { Send, X, Loader2 } from "lucide-react";
import SuccessMessage from "../messages/SuccessMessage";
import FailMessage from "../messages/FailMessage";
import { testEmailTemplate } from "../../api/apiEmailTemplates";
import { useUser } from "../../context/UserContext";

interface ViewEmailTemplateModalProps {
  isOpen: boolean;
  id: number;
  name: string;
  subject: string;
  html: string;
  onClose: () => void;
}

const ViewEmailTemplateModal = ({
  isOpen,
  id,
  name,
  subject,
  html,
  onClose,
}: ViewEmailTemplateModalProps) => {
  const [isSending, setIsSending] = useState(false);
  const [testEmail, setTestEmail] = useState("");
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);

  if (!isOpen) return null;
  const { hasRole } = useUser();

  const replaceVariables = (text: string): string =>
    (text || "")
      .replace(/{FirstName}/g, "John")
      .replace(/{LastName}/g, "Doe")
      .replace(/{CompanyName}/g, "Operimus")
      .replace(/{Email}/g, "john@operimus.com");

  const previewSubject = replaceVariables(subject);
  const previewHtml = `<!DOCTYPE html>
  <html>
    <head>
      <meta charset="UTF-8" />
      <link rel="stylesheet" href="https://editor.unlayer.com/assets/css/unlayer-preview.css" />
      <style>
        body {
          background: #fff;
          margin: 0;
          padding: 0;
          font-family: Inter, Arial, sans-serif;
        }
      </style>
    </head>
    <body>
      ${
        replaceVariables(html) ||
        "<p style='color:red;text-align:center;margin-top:20px;'>No content available</p>"
      }
    </body>
  </html>`;

  const handleTestEmail = async () => {
    if (!id) {
      setToastMessage({ type: "fail", text: "Missing template ID" });
      setTimeout(() => setToastMessage(null), 3000);
      return;
    }

    if (!testEmail || !testEmail.trim()) {
      setToastMessage({ type: "fail", text: "Please enter an email address" });
      setTimeout(() => setToastMessage(null), 3000);
      return;
    }

    setIsSending(true);
    try {
      await testEmailTemplate(id, testEmail);
      setToastMessage({
        type: "success",
        text: `Test email sent successfully to ${testEmail}`,
      });
      setTestEmail("");
    } catch (err: any) {
      console.error("Failed to send test email", err);
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        "Failed to send test email";
      setToastMessage({ type: "fail", text: msg });
    } finally {
      setIsSending(false);
      setTimeout(() => setToastMessage(null), 3000);
    }
  };

  return (
    <>
      {toastMessage && (
        <>
          {toastMessage.type === "success" ? (
            <SuccessMessage
              entity={toastMessage.text}
              onClose={() => setToastMessage(null)}
            />
          ) : (
            <FailMessage
              entity={toastMessage.text}
              onClose={() => setToastMessage(null)}
            />
          )}
        </>
      )}

      <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div className="bg-white w-[900px] max-h-[90vh] rounded-2xl shadow-xl p-8 overflow-y-auto animate-fadeIn relative">
          <div className="flex justify-between items-center mb-5">
            <div>
              <h2 className="text-xl font-semibold text-[#111827]">{name}</h2>
              <p className="text-sm text-[#6B7280]">
                <b>Subject:</b> {previewSubject || "No subject"}
              </p>
            </div>
            <button
              onClick={onClose}
              className="text-[#6B7280] hover:text-[#111827] p-2 rounded-md hover:bg-gray-100 transition-all cursor-pointer"
              title="Close"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <div className="border border-[#E5E7EB] rounded-lg overflow-hidden bg-white shadow-inner">
            <iframe
              title="Email Preview"
              srcDoc={previewHtml}
              className="w-full h-[650px] border-0 rounded-md"
            />
          </div>

          {/* Test Email Address section - Only visible to CONTENT_MARKETER or MARKETING_MANAGER */}
          {(hasRole("CONTENT_MARKETER") || hasRole("MARKETING_MANAGER")) && (
            <div className="mt-6 p-4 bg-gray-50 rounded-lg border border-[#E5E7EB]">
              <label className="block text-sm font-medium text-[#111827] mb-2">
                Test Email Address
              </label>
              <input
                type="email"
                value={testEmail}
                onChange={(e) => setTestEmail(e.target.value)}
                placeholder="Enter email address to send test to"
                className="w-full px-3 py-2 border border-[#D1D5DB] rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB]"
                disabled={isSending}
              />
            </div>
          )}

          <div className="flex justify-end gap-3 mt-8">
            <button
              onClick={onClose}
              className="border border-[#D1D5DB] text-[#111827] hover:bg-[#F9FAFB] px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm flex items-center gap-2 cursor-pointer"
            >
              <X className="h-4 w-4" />
              Close
            </button>

            {(hasRole("CONTENT_MARKETER") || hasRole("MARKETING_MANAGER")) && (
              <button
                onClick={handleTestEmail}
                disabled={isSending || !id || !testEmail.trim()}
                className={`flex items-center gap-2 ${
                  isSending || !testEmail.trim()
                    ? "bg-blue-400 cursor-not-allowed"
                    : "bg-[#2563EB] hover:bg-[#1D4ED8]"
                } text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm cursor-pointer`}
              >
                {isSending ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Sending...
                  </>
                ) : (
                  <>
                    <Send className="h-4 w-4" />
                    {!testEmail.trim() ? "Enter email first" : "Send Test Email"}
                  </>
                )}
              </button>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default ViewEmailTemplateModal;
