/** Spring's `Page<T>` envelope, as serialized by Jackson. */
export interface SpringPage<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
