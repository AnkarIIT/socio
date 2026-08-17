import { Controller, Get, Query } from '@nestjs/common';
import { SearchQuerySchema } from '@bharat/contracts';
import type { SearchQuery, SearchResult } from '@bharat/contracts';
import { Public } from '../../common/decorators/public.decorator';
import { ZodValidationPipe } from '../../common/pipes/zod-validation.pipe';
import { SearchService } from './search.service';

@Controller('search')
export class SearchController {
  constructor(private readonly searchService: SearchService) {}

  @Public()
  @Get()
  search(
    @Query(new ZodValidationPipe(SearchQuerySchema)) query: SearchQuery,
  ): Promise<SearchResult> {
    return this.searchService.search(query);
  }
}
