interface DeleteConfirmModalProps {
  isOpen: boolean;
  title?: string;
  message?: string;
  entityName?: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const DeleteConfirmModal = ({
  isOpen,
  title = "Confirm Deletion",
  message,
  entityName,
  confirmText = "Yes, Delete",
  cancelText = "Cancel",
  onConfirm,
  onCancel,
}: DeleteConfirmModalProps) => {
  if (!isOpen) return null;

  const defaultMessage = `Are you sure you want to delete ${
    entityName ? `"${entityName}"` : "this item"
  }? This action cannot be undone.`;

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
      <div className="bg-white w-[420px] rounded-xl shadow-lg p-6 text-center animate-fadeIn">
        <h2 className="text-lg font-semibold text-[#B91C1C] mb-2">{title}</h2>

        <p className="text-sm text-[#6B7280] mb-6">
          {message || defaultMessage}
        </p>

        <div className="flex justify-center gap-3">
          <button
            onClick={onCancel}
            className="px-4 py-2 text-sm font-medium text-[#374151] bg-[#F3F4F6] hover:bg-[#E5E7EB] rounded-md cursor-pointer transition"
          >
            {cancelText}
          </button>

          <button
            onClick={onConfirm}
            className="px-4 py-2 text-sm font-medium text-white bg-[#DC2626] hover:bg-[#B91C1C] rounded-md cursor-pointer transition"
          >
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default DeleteConfirmModal;
