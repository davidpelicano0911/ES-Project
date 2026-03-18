interface SavePreviewModalProps {
  isOpen: boolean;
  name: string;
  body: string;
  onConfirm: () => void;
  onCancel: () => void;

  subject?: string;
  description?: string;
  fontSize?: number;
  color?: string;
  fontFamily?: string;
  title?: string;
  confirmText?: string;
  showCancel?: boolean;
}

const SavePreviewModal = ({
  isOpen,
  title,
  name,
  subject,
  description,
  body,
  fontSize,
  color,
  fontFamily,
  confirmText,
  showCancel = true,
  onConfirm,
  onCancel,
}: SavePreviewModalProps) => {
  if (!isOpen) return null;

  const replaceVariables = (text: string): string =>
    (text || "")
      .replace(/{FirstName}/g, "John")
      .replace(/{LastName}/g, "Doe")
      .replace(/{CompanyName}/g, "Operimus")
      .replace(/{Email}/g, "john@operimus.com");

  const previewSubject = replaceVariables(subject || "");
  const previewName = replaceVariables(name || "");
  const previewDescription = replaceVariables(description || "");

  const isEmailPreview = !!subject;
  const resolvedTitle =
    title || (isEmailPreview ? "Email Template Preview" : "Landing Page Preview");
  const resolvedConfirmText =
    confirmText || (isEmailPreview ? "Save Template" : "Save Landing Page");

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white w-[900px] max-h-[90vh] rounded-2xl shadow-xl p-8 overflow-y-auto animate-fadeIn relative">
        <div className="flex justify-between items-center mb-5">
          <div>
            <h2 className="text-xl font-semibold text-[#111827]">
              {resolvedTitle}
            </h2>
            <p className="text-sm text-[#6B7280]">
              {isEmailPreview
                ? "Preview of how your email will look with example values."
                : "Preview of how your landing page will appear to visitors."}
            </p>
          </div>
          <button
            onClick={onCancel}
            className="text-[#6B7280] hover:text-[#111827] text-lg font-medium transition"
          >
            ✕
          </button>
        </div>

        <div className="border border-[#E5E7EB] rounded-lg p-4 bg-[#F9FAFB] mb-6">
          {isEmailPreview ? (
            <>
              <h3 className="text-[#111827] text-base font-medium mb-1">
                <span className="font-semibold">Subject:</span>{" "}
                {previewSubject || "No subject"}
              </h3>
              <p className="text-sm text-[#6B7280]">
                <span className="font-semibold">Template Name:</span>{" "}
                {previewName || "Untitled"}
              </p>
            </>
          ) : (
            <>
              <h3 className="text-[#111827] text-base font-medium mb-1">
                <span className="font-semibold">Landing Page:</span>{" "}
                {previewName || "Untitled"}
              </h3>
              {description && (
                <p className="text-sm text-[#6B7280]">
                  <span className="font-semibold">Description:</span>{" "}
                  {previewDescription}
                </p>
              )}
            </>
          )}
        </div>

        <div className="border border-[#E5E7EB] rounded-lg overflow-hidden bg-white">
          <iframe
            title="Preview"
            className="w-full h-[650px] border-0 rounded-lg shadow-inner"
            srcDoc={`<!DOCTYPE html>
            <html>
              <head>
                <meta charset="UTF-8" />
                <link rel="stylesheet" href="https://editor.unlayer.com/assets/css/unlayer-preview.css" />
                <style>
                  body {
                    background: #ffffff;
                    font-family: ${
                      fontFamily || "Inter, Arial, sans-serif"
                    };
                    font-size: ${fontSize || 16}px;
                    color: ${color || "#000000"};
                    margin: 0;
                    padding: 0;
                  }
                </style>
              </head>
              <body>
                ${
                  body ||
                  "<p style='color:red;text-align:center;margin-top:20px;'>Failed to export HTML — no valid content found.</p>"
                }
              </body>
            </html>`}
          />
        </div>

        <div className="flex justify-end gap-3 mt-8">
          {showCancel && (
            <button
              onClick={onCancel}
              className="border border-[#D1D5DB] text-[#111827] hover:bg-[#F9FAFB] px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm cursor-pointer"
            >
              Cancel
            </button>
          )}
          <button
            onClick={onConfirm}
            className="bg-[#2563EB] hover:bg-[#1D4ED8] text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm cursor-pointer"
          >
            {resolvedConfirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default SavePreviewModal;