import { useState } from "react";

interface ImageUploaderProps {
  onFileSelect: (file: File) => void;
}

const ImageUploader = ({ onFileSelect }: ImageUploaderProps) => {
  const [file, setFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  const allowedExtensions = [".jpg", ".jpeg", ".png", ".gif", ".webp"];

  const handleFile = (selectedFile: File | undefined) => {
    if (!selectedFile) return;

    const ext = selectedFile.name
      .toLowerCase()
      .slice(selectedFile.name.lastIndexOf("."));

    if (!allowedExtensions.includes(ext)) {
      alert(
        "Only image files (.jpg, .jpeg, .png, .gif, .webp) are allowed."
      );
      return;
    }

    setFile(selectedFile);
    setPreviewUrl(URL.createObjectURL(selectedFile));
    onFileSelect(selectedFile); // pass file to parent
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFile(e.target.files?.[0]);
    e.target.value = ""; // reset input so same file can be re-selected
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    handleFile(e.dataTransfer.files?.[0]);
  };

  return (
    <div
      onDrop={handleDrop}
      onDragOver={(e) => e.preventDefault()}
      onClick={() => document.getElementById("fileInput")?.click()}
      className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center cursor-pointer hover:bg-gray-50 transition"
    >
      <input
        id="fileInput"
        type="file"
        accept={allowedExtensions.join(",")}
        onChange={handleFileSelect}
        className="hidden"
      />

      {previewUrl ? (
        <div className="flex flex-col items-center space-y-2">
          <img
            src={previewUrl}
            alt="Preview"
            className="w-40 h-40 object-cover rounded-lg border"
          />
          <p className="text-sm text-gray-700">{file?.name}</p>
        </div>
      ) : (
        <p className="text-gray-500">
          Drag & drop an image here, or{" "}
          <span className="text-blue-600 underline">browse</span>
        </p>
      )}
    </div>
  );
};

export default ImageUploader;
