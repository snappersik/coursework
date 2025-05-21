import React, { useEffect, useState } from 'react';
import { observer } from 'mobx-react-lite';
import ProductCard from '../../components/catalog/ProductCard';
import FilterButtons from '../../components/catalog/FilterButtons';
import apiClient from '../../api/apiClient';
import { FaSearch } from 'react-icons/fa';
import { cartStore } from '../../store/cartStore';

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

  useEffect(() => { fetchCategories(); }, []);
  useEffect(() => { fetchProducts(); }, [filter, searchTerm, currentPage]);

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
      if (searchTerm) url += `&name=${encodeURIComponent(searchTerm)}`;
      if (filter !== 'all') url += `&category=${encodeURIComponent(filter)}`;
      const response = await apiClient.get(url);
      setProducts(Array.isArray(response.data.content) ? response.data.content : []);
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
    setCurrentPage(0);
  };

  const handleSearchChange = (e) => setSearchTerm(e.target.value);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchProducts();
  };

  const handlePageChange = (newPage) => setCurrentPage(newPage);

  const handleAddToCart = async (product) => {
    try {
      await cartStore.addItem(product);
    } catch (error) {
      alert(error.message);
    }
  };

  const isInCart = (productId) => cartStore.items.some(item => item.id === productId);

  if (loading && products.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#424242] py-10 px-2 md:px-8">
      <h1 className="text-3xl font-bold mb-6 text-yellow-500">Каталог товаров</h1>
      <form onSubmit={handleSearchSubmit} className="mb-6 flex flex-col md:flex-row gap-4 items-center">
        <div className="relative flex-1">
          <input
            type="text"
            placeholder="Поиск по названию..."
            value={searchTerm}
            onChange={handleSearchChange}
            className="w-full p-3 pl-10 bg-[#525252] border border-gray-700 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-yellow-500 transition-all duration-300"
          />
          <FaSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        </div>
        <button
          type="submit"
          className="px-6 py-3 bg-yellow-500 hover:bg-yellow-600 rounded-lg text-white font-bold transition-colors"
        >
          Поиск
        </button>
      </form>
      <FilterButtons
        activeFilter={filter}
        onFilterChange={handleFilterChange}
        categories={categories}
      />
      {error && (
        <div className="bg-red-600 text-white p-3 rounded mb-6">{error}</div>
      )}
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-6">
        {products.length === 0 ? (
          <div className="col-span-full text-center text-gray-300 text-xl p-10 bg-[#525252] rounded-lg">
            Продукты не найдены
            <div className="text-gray-400 text-base mt-2">Попробуйте изменить параметры поиска</div>
          </div>
        ) : (
          products.map(product => (
            <ProductCard
              key={product.id}
              product={product}
              inCart={isInCart(product.id)}
              onAddToCart={handleAddToCart}
            />
          ))
        )}
      </div>
      {/* Пагинация */}
      {totalPages > 1 && (
        <div className="flex justify-center mt-8 gap-2">
          {[...Array(totalPages).keys()].map(pageNum => (
            <button
              key={pageNum}
              onClick={() => handlePageChange(pageNum)}
              className={`px-4 py-2 rounded-lg font-bold transition-colors ${
                currentPage === pageNum
                  ? 'bg-yellow-500 text-white'
                  : 'bg-[#525252] text-gray-300 hover:bg-yellow-600 hover:text-white'
              }`}
            >
              {pageNum + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  );
});

export default CatalogPage;
