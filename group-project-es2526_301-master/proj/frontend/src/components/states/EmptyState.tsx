interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description: string;
}

const EmptyState = ({ icon, title, description }: EmptyStateProps) => (
  <div className="text-center flex flex-col items-center justify-center">
    <div className="text-blue-600 mb-4">
      {icon && <div>{icon}</div>}
    </div>
    <h2 className="text-lg font-semibold text-gray-800 mb-1">{title}</h2>
    <p className="text-gray-500 text-sm max-w-md">{description}</p>
  </div>
);

export default EmptyState;