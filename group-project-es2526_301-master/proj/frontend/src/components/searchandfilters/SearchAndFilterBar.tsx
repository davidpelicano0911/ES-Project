import SearchBar from "./SearchBar";

interface SearchAndFilterBarProps<T extends string = string> {
  search: string;
  onSearchChange: (value: string) => void;
  filter: T;
  onFilterChange: (value: T) => void;
  filters?: T[];
  placeholder?: string;
}

const SearchAndFilterBar = <T extends string = string>({
  search,
  onSearchChange,
  filter,
  onFilterChange,
  filters = ["All", "Active", "In Progress", "Finished"] as unknown as T[],
  placeholder = "Search...",
}: SearchAndFilterBarProps<T>) => {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center gap-4 mb-6">
      <div className="flex items-center gap-3 w-full sm:w-auto">
        <SearchBar
          value={search}
          onChange={onSearchChange}
          placeholder={placeholder}
        />
        <div className="flex items-center gap-1">
          {filters.map((tab) => (
            <button
              key={tab}
              onClick={() => onFilterChange(tab)}
              className={`px-4 py-1.5 text-sm font-medium rounded-md border transition-all ${
                filter === tab
                  ? "bg-[#2563EB] text-white border-[#2563EB]"
                  : "bg-white text-[#374151] border-[#E5E7EB] hover:bg-[#F3F4F6]"
              }`}
            >
              {tab}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default SearchAndFilterBar;
