import React, { useState, useEffect, useCallback } from 'react'
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd'
import { useDropzone } from 'react-dropzone'
import axios from 'axios'
import { toast } from 'react-toastify'
import { API_URL } from '../../../config'
import apiClient from '../../../api/apiClient'

const SliderManager = () => {
  const [sliderItems, setSliderItems] = useState([])
  const [allBooks, setAllBooks] = useState([])
  const [filter, setFilter] = useState('')
  const [loading, setLoading] = useState(true)
  const [descriptions, setDescriptions] = useState({})
  const [bgFiles, setBgFiles] = useState({})
  const [bgUrls, setBgUrls] = useState({})
  const [activeUploadId, setActiveUploadId] = useState(null)

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    setLoading(true)
    try {
      const [sliderRes, booksRes] = await Promise.all([
        fetchSliderBooks(),
        apiClient.get('/books')
      ])
      setSliderItems(sliderRes.data)
      setAllBooks(booksRes.data)
      const initDesc = {}
      sliderRes.data.forEach(item => {
        initDesc[item.id] = item.customDescription || ''
      })
      setDescriptions(initDesc)
    } catch {
      toast.error('Не удалось загрузить данные')
    }
    setLoading(false)
  }

  const fetchSliderBooks = async () => {
    return axios.get(`${API_URL}/slider/admin/books`, { withCredentials: true })
  }

  const onDrop = useCallback(files => {
    if (activeUploadId !== null) {
      setBgFiles(prev => ({ ...prev, [activeUploadId]: files[0] }))
    }
  }, [activeUploadId])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: false
  })

  const handleDragEnd = result => {
    if (!result.destination) return
    const items = Array.from(sliderItems)
    const [moved] = items.splice(result.source.index, 1)
    items.splice(result.destination.index, 0, moved)
    setSliderItems(items)
    saveOrder(items)
  }

  const saveOrder = async items => {
    try {
      const ids = items.map(i => i.id)
      await axios.post(
        `${API_URL}/slider/admin/books/reorder`,
        ids,
        { withCredentials: true }
      )
      toast.success('Порядок обновлён')
    } catch {
      toast.error('Ошибка сохранения порядка')
    }
  }

  const addSliderBook = async (bookId) => {
    return axios.post(`${API_URL}/slider/admin/books`, { bookId }, { withCredentials: true })
  }

  const deleteSliderBook = async (id) => {
    return axios.delete(`${API_URL}/slider/admin/books/${id}`, { withCredentials: true })
  }

  const updateSliderDesc = async (id, description) => {
    return axios.put(`${API_URL}/slider/admin/books/${id}/description`, 
      { description }, 
      { withCredentials: true }
    )
  }

  const uploadSliderBgFile = async (id, file) => {
    const formData = new FormData()
    formData.append('file', file)
    return axios.post(
      `${API_URL}/slider/admin/books/${id}/background-file`,
      formData,
      {
        withCredentials: true,
        headers: { 'Content-Type': 'multipart/form-data' }
      }
    )
  }

  const setSliderBgUrl = async (id, url) => {
    return axios.put(
      `${API_URL}/slider/admin/books/${id}/background-url`,
      { url },
      { withCredentials: true }
    )
  }

  const addBook = async id => {
    if (!id) return
    try {
      const { data } = await addSliderBook(+id)
      setSliderItems([data,...sliderItems])
      toast.success('Книга добавлена')
    } catch (error) {
      console.error('Ошибка добавления книги в слайдер:', error)
      toast.error(`Не удалось добавить книгу: ${error.message}`)
    }
  }

  const removeItem = async id => {
    if (!confirm('Удалить из слайдера?')) return
    try {
      await deleteSliderBook(id)
      setSliderItems(sliderItems.filter(i=>i.id!==id))
      toast.success('Удалено')
    } catch (error) {
      console.error('Ошибка удаления из слайдера:', error)
      toast.error(`Не удалось удалить: ${error.message}`)
    }
  }

  const saveDescription = async id => {
    try {
      await updateSliderDesc(id, descriptions[id])
      toast.success('Описание обновлено')
    } catch (error) {
      console.error('Ошибка сохранения описания слайдера:', error)
      toast.error(`Не удалось сохранить описание: ${error.message}`)
    }
  }

  const uploadBackground = async id => {
    const file = bgFiles[id]
    if (!file) return
    try {
      await uploadSliderBgFile(id, file)
      toast.success('Фон загружен')
    } catch (error) {
      console.error('Ошибка загрузки фона слайдера:', error)
      toast.error(`Не удалось загрузить фон: ${error.message}`)
    }
  }

  const setBackgroundUrl = async id => {
    const url = bgUrls[id]
    if (!url) return
    try {
      await setSliderBgUrl(id, url)
      toast.success('URL сохранён')
    } catch (error) {
      console.error('Ошибка сохранения URL фона:', error)
      toast.error(`Не удалось сохранить URL: ${error.message}`)
    }
  }

  const filtered = sliderItems.filter(item => !filter || item.id.toString().includes(filter))

  if (loading) {
    return (
      <div className="p-4 flex justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-yellow-500"></div>
      </div>
    )
  }

  return (
    <div className="p-4">
      <h2 className="text-2xl font-bold mb-4">Управление слайдером</h2>
      
      <div className="mb-6 flex items-center">
        <input
          type="text"
          placeholder="Фильтр по ID"
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="p-2 border rounded mr-4"
        />
        
        <div className="flex-1">
          <select
            className="p-2 border rounded w-64"
            onChange={e => addBook(e.target.value)}
            value=""
          >
            <option value="">Добавить книгу в слайдер</option>
            {allBooks.filter(b => !sliderItems.some(s => s.book.id === b.id)).map(book => (
              <option key={book.id} value={book.id}>
                {book.id} - {book.title} ({book.author})
              </option>
            ))}
          </select>
        </div>
      </div>

      <DragDropContext onDragEnd={handleDragEnd}>
        <Droppable droppableId="slider-items">
          {(provided) => (
            <div
              {...provided.droppableProps}
              ref={provided.innerRef}
              className="space-y-4"
            >
              {filtered.map((item, index) => (
                <Draggable key={item.id} draggableId={item.id.toString()} index={index}>
                  {(provided) => (
                    <div
                      ref={provided.innerRef}
                      {...provided.draggableProps}
                      className="bg-gray-800 p-4 rounded-lg shadow"
                    >
                      <div className="flex flex-col md:flex-row">
                        <div
                          {...provided.dragHandleProps}
                          className="flex-shrink-0 mr-4 cursor-move p-2 bg-gray-700 rounded mb-4 md:mb-0"
                        >
                          <div className="w-8 h-8 flex items-center justify-center">
                            <span className="text-xl">≡</span>
                          </div>
                        </div>
                        
                        <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
                          <div>
                            <h3 className="text-xl font-bold">
                              {item.book.title} (ID: {item.id})
                            </h3>
                            <p className="text-gray-400">{item.book.author}</p>
                            
                            <div className="mt-4">
                              <label className="block text-sm font-medium text-gray-400 mb-1">
                                Описание для слайдера
                              </label>
                              <textarea
                                value={descriptions[item.id] || ''}
                                onChange={e => setDescriptions({
                                  ...descriptions,
                                  [item.id]: e.target.value
                                })}
                                className="w-full p-2 bg-gray-700 border border-gray-600 rounded"
                                rows="3"
                              ></textarea>
                              <button
                                onClick={() => saveDescription(item.id)}
                                className="mt-2 px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700"
                              >
                                Сохранить описание
                              </button>
                            </div>
                          </div>
                          
                          <div>
                            <div className="mb-4">
                              <label className="block text-sm font-medium text-gray-400 mb-1">
                                Загрузить фоновое изображение
                              </label>
                              <div
                                onClick={() => setActiveUploadId(item.id)}
                                {...getRootProps()}
                                className={`border-2 border-dashed p-4 rounded text-center cursor-pointer ${
                                  isDragActive && activeUploadId === item.id
                                    ? 'border-yellow-500 bg-gray-700'
                                    : 'border-gray-600'
                                }`}
                              >
                                <input {...getInputProps()} />
                                {bgFiles[item.id] ? (
                                  <p>{bgFiles[item.id].name}</p>
                                ) : (
                                  <p>Перетащите файл или кликните для выбора</p>
                                )}
                              </div>
                              {bgFiles[item.id] && (
                                <button
                                  onClick={() => uploadBackground(item.id)}
                                  className="mt-2 px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700"
                                >
                                  Загрузить фон
                                </button>
                              )}
                            </div>
                            
                            <div className="mb-4">
                              <label className="block text-sm font-medium text-gray-400 mb-1">
                                Или укажите URL фона
                              </label>
                              <input
                                type="text"
                                value={bgUrls[item.id] || ''}
                                onChange={e => setBgUrls({
                                  ...bgUrls,
                                  [item.id]: e.target.value
                                })}
                                className="w-full p-2 bg-gray-700 border border-gray-600 rounded"
                                placeholder="https://..."
                              />
                              <button
                                onClick={() => setBackgroundUrl(item.id)}
                                className="mt-2 px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700"
                              >
                                Сохранить URL
                              </button>
                            </div>
                            
                            <button
                              onClick={() => removeItem(item.id)}
                              className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700"
                            >
                              Удалить из слайдера
                            </button>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                </Draggable>
              ))}
              {provided.placeholder}
            </div>
          )}
        </Droppable>
      </DragDropContext>
    </div>
  )
}

export default SliderManager
