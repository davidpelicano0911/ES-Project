interface ConfirmationModalProps {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmationModal = ({
  title,
  message,
  confirmText = "Confirm",
  cancelText = "Cancel",
  onConfirm,
  onCancel,
}: ConfirmationModalProps) => {
  return (
    <div className="fixed inset-0 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-2xl border border-[#E5E7EB] p-8 w-full max-w-md animate-fadeIn">
        <h2 className="text-lg font-semibold text-[#111827] mb-3">{title}</h2>
        <p className="text-sm text-[#6B7280] mb-6">{message}</p>

        <div className="flex justify-end gap-3">
          <button
            onClick={onCancel}
            className="border border-[#E5E7EB] text-[#374151] px-4 py-2 rounded-lg hover:bg-[#F3F4F6] transition"
          >
            {cancelText}
          </button>
          <button
            onClick={onConfirm}
            className="bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-medium px-4 py-2 rounded-lg transition"
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
