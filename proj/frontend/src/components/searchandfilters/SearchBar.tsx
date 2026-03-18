import { Search } from "lucide-react";

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

const SearchBar = ({ value, onChange, placeholder = "Search campaigns..." }: SearchBarProps) => {
  return (
    <div className="relative flex-1 max-w-xs">
      <Search className="absolute left-3 top-2.5 w-5 h-5 text-gray-400" />
      <input
        type="text"
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full pl-10 pr-3 py-2 border border-[#E5E7EB] rounded-md text-sm text-gray-700 bg-white focus:ring-2 focus:ring-[#2563EB] focus:outline-none placeholder:text-gray-400"
      />
    </div>
  );
};

export default SearchBar;