import React, { useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import ProductCard from '../../components/catalog/ProductCard';
import FilterButtons from '../../components/catalog/FilterButtons';
import apiClient from '../../api/apiClient';
import { FaSearch } from 'react-icons/fa';

const CatalogPage = observer(() => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [categories, setCategories] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const pageSize = 12;

  useEffect(() => {
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchProducts();
  }, [filter, searchTerm, currentPage]);

  const fetchCategories = async () => {
    try {
      const response = await apiClient.get('/products/categories/with-descriptions');
      setCategories(response.data);
    } catch (err) {
      console.error('Ошибка при загрузке категорий:', err);
    }
  };

  const fetchProducts = async () => {
    setLoading(true);
    try {
      let url = `/products/search?page=${currentPage}&size=${pageSize}`;

      if (searchTerm) {
        url += `&name=${encodeURIComponent(searchTerm)}`;
      }

      if (filter !== 'all') {
        url += `&category=${encodeURIComponent(filter)}`;
      }

      const response = await apiClient.get(url);
      setProducts(response.data.content);
      setTotalPages(response.data.totalPages);
      setError('');
    } catch (err) {
      console.error('Ошибка при загрузке продуктов:', err);
      setError('Не удалось загрузить продукты');
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (newFilter) => {
    setFilter(newFilter);
    setCurrentPage(0); // Сброс на первую страницу при изменении фильтра
  };

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setCurrentPage(0); // Сброс на первую страницу при поиске
    fetchProducts();
  };

  const handlePageChange = (newPage) => {
    setCurrentPage(newPage);
  };

  if (loading && products.length === 0) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-yellow-400"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-6 text-center">Каталог продуктов</h1>

      {/* Поисковая строка */}
      <div className="mb-6">
        <form onSubmit={handleSearchSubmit} className="flex w-full max-w-md mx-auto">
          <input
            type="text"
            placeholder="Поиск продуктов..."
            value={searchTerm}
            onChange={handleSearchChange}
            className="flex-grow px-4 py-2 border border-gray-300 rounded-l focus:outline-none focus:ring-2 focus:ring-yellow-400"
          />
          <button
            type="submit"
            className="bg-yellow-400 hover:bg-yellow-500 text-white px-4 py-2 rounded-r"
          >
            <FaSearch />
          </button>
        </form>
      </div>

      {/* Фильтры категорий */}
      <div className="mb-8">
        <FilterButtons
          categories={categories}
          activeFilter={filter}
          onFilterChange={handleFilterChange}
        />
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {products.length === 0 && !loading ? (
        <div className="text-center py-8">
          <p className="text-lg text-gray-600">Продукты не найдены</p>
          <p className="text-sm text-gray-500 mt-2">Попробуйте изменить параметры поиска</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {products.map(product => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>

          {/* Пагинация */}
          {totalPages > 1 && (
            <div className="flex justify-center mt-8">
              <nav className="inline-flex rounded-md shadow">
                <button
                  onClick={() => handlePageChange(Math.max(0, currentPage - 1))}
                  disabled={currentPage === 0}
                  className="px-3 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                >
                  Назад
                </button>
                {[...Array(totalPages).keys()].map(page => (
                  <button
                    key={page}
                    onClick={() => handlePageChange(page)}
                    className={`px-3 py-2 border border-gray-300 text-sm font-medium ${currentPage === page
                        ? 'bg-yellow-400 text-white'
                        : 'bg-white text-gray-500 hover:bg-gray-50'
                      }`}
                  >
                    {page + 1}
                  </button>
                ))}
                <button
                  onClick={() => handlePageChange(Math.min(totalPages - 1, currentPage + 1))}
                  disabled={currentPage === totalPages - 1}
                  className="px-3 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:opacity-50"
                >
                  Вперед
                </button>
              </nav>
            </div>
          )}
        </>
      )}
    </div>
  );
});

export default CatalogPage;
