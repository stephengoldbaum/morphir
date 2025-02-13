from typing import Type, TypeVar, Optional, List
from abc import ABC, abstractmethod
from datathread import Identifier

T = TypeVar('T')

class Metastore(ABC):
    @abstractmethod
    def read(self, id: Identifier, tipe: Type[T]) -> Optional[T]:
        pass

    @abstractmethod
    def read_all(self, tipe: Type[T]) -> List[T]:
        pass

    @abstractmethod
    def write(self, id: Identifier, data: T) -> Optional[str]:
        pass

    @abstractmethod
    def delete(self, id: Identifier) -> Optional[str]:
        pass
